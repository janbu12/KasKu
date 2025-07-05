const { dbFirestore, admin } = require("../config/firebase");
const { Receipt } = require("../models/User");

// Add a receipt to a user (expects req.body.uid and req.body.receipt)
exports.addStruct = async (req, res) => {
  const { uid, receipt } = req.body;
  if (!uid || !receipt) {
    return res.status(400).json({ message: "User ID (uid) and receipt data are required." });
  }
  try {
    const userRef = dbFirestore.collection('users').doc(uid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    // Generate Firestore UUID
    const receiptId = dbFirestore.collection('_').doc().id;
    // Pastikan id masuk ke receipt
    const receiptWithId = { ...receipt, id: receiptId };
    // Convert receipt plain object to Receipt instance, then to Firestore format
    const receiptInstance = Receipt.fromFirestore(receiptWithId);
    await userRef.update({
      receipts: admin.firestore.FieldValue.arrayUnion(receiptInstance.toFirestore())
    });
    res.status(200).json({ message: "Receipt added successfully.", id: receiptId });
  } catch (error) {
    res.status(500).json({ message: "Failed to add receipt.", error: error.message });
  }
};

// Edit a receipt by id (expects req.params.id, req.body.uid, req.body.receipt)
exports.editStruct = async (req, res) => {
  const { id } = req.params;
  const { uid, receipt } = req.body;
  if (!uid || !id || !receipt) {
    return res.status(400).json({ message: "User ID (uid), receipt data, and struct id are required." });
  }
  try {
    const userRef = dbFirestore.collection('users').doc(uid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receipts = userDoc.data().receipts || [];
    const idx = receipts.findIndex(r => r.id === id);
    if (idx === -1) {
      return res.status(404).json({ message: "Receipt not found." });
    }
    // Replace the old receipt with the new one (ensure id stays the same)
    receipts[idx] = { ...receipts[idx], ...receipt, id };
    await userRef.update({ receipts });
    res.status(200).json({ message: "Receipt updated successfully." });
  } catch (error) {
    res.status(500).json({ message: "Failed to update receipt.", error: error.message });
  }
};

// Delete a receipt by id (expects req.params.id, req.body.uid)
exports.deleteStruct = async (req, res) => {
  const { id } = req.params;
  const { uid } = req.body;
  if (!uid || !id) {
    return res.status(400).json({ message: "User ID (uid) and struct id are required." });
  }
  try {
    const userRef = dbFirestore.collection('users').doc(uid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receipts = userDoc.data().receipts || [];
    const newReceipts = receipts.filter(r => r.id !== id);
    if (newReceipts.length === receipts.length) {
      return res.status(404).json({ message: "Receipt not found." });
    }
    await userRef.update({ receipts: newReceipts });
    res.status(200).json({ message: "Receipt deleted successfully." });
  } catch (error) {
    res.status(500).json({ message: "Failed to delete receipt.", error: error.message });
  }
};

// Get all receipts for a user (expects req.params.userUid)
exports.getMyStructs = async (req, res) => {
  const { userUid } = req.params;
  if (!userUid) {
    return res.status(400).json({ message: "User UID is required." });
  }
  try {
    const userRef = dbFirestore.collection('users').doc(userUid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receipts = userDoc.data().receipts || [];
    res.status(200).json(receipts);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch receipts.", error: error.message });
  }
};
