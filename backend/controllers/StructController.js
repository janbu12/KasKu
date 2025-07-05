const { admin } = require("../config/firebase");
const { Receipt } = require("../models/User");
const redisClient = require('../config/redis'); // tambahkan import redis

// Add a receipt to a user (expects req.body.uid and req.body.receipt)
exports.addStruct = async (req, res) => {
  const authenticatedUserUid = req.user.uid;
  const db = req.db;
  const { receipt } = req.body;

  if (!receipt) {
    return res.status(400).json({ message: "receipt data required." });
  }

  try {
    const userRef = db.collection('users').doc(authenticatedUserUid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receiptId = db.collection('_').doc().id;
    const receiptWithId = { ...receipt, id: receiptId };
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
  const { receipt } = req.body;
  const authenticatedUserUid = req.user.uid;
  const db = req.db;

  if (!id || !receipt) {
    return res.status(400).json({ message: "User ID (uid), receipt data, and struct id are required." });
  }
  try {
    const userRef = db.collection('users').doc(authenticatedUserUid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receipts = userDoc.data().receipts || [];
    const idx = receipts.findIndex(r => r.id === id);
    if (idx === -1) {
      return res.status(404).json({ message: "Receipt not found." });
    }

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
  const authenticatedUserUid = req.user.uid;
  const db = req.db;

  if (!id) {
    return res.status(400).json({ message: "User ID (uid) and struct id are required." });
  }

  try {
    const userRef = db.collection('users').doc(authenticatedUserUid);
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
  const authenticatedUserUid = req.user.uid;
  const db = req.db;

  try {
    const cacheKey = `user:structs:${authenticatedUserUid}`;
    const cachedStructs = await redisClient.get(cacheKey);
    if (cachedStructs) {
      return res.status(200).json(JSON.parse(cachedStructs));
    }

    const userRef = db.collection('users').doc(authenticatedUserUid);
    const userDoc = await userRef.get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const receipts = userDoc.data().receipts || [];

    // Simpan ke Redis selama 5 menit (300 detik)
    await redisClient.setEx(cacheKey, 300, JSON.stringify(receipts));

    res.status(200).json(receipts);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch receipts.", error: error.message });
  }
};
