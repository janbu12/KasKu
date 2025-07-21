const { admin, dbFirestore } = require("../config/firebase");
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

  if (!receipt.merchant_name || !receipt.transaction_date || !receipt.transaction_time || !receipt.items || !receipt.final_total) {
    return res.status(400).json({ message: "Merchant name, transaction date, transaction time, items, and final total are required." });
  }

  if (!receipt.tender_type || !receipt.category_spending) {
    return res.status(400).json({ message: "Tender type or category spending are required." });
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

    // Clear the cache for this user's receipts
    // This ensures that the next time receipts are fetched, they will be retrieved from the database
    const cacheKey = `user:structs:${authenticatedUserUid}`;
    await redisClient.del(cacheKey);

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

  if (!receipt.merchant_name || !receipt.transaction_date || !receipt.transaction_time || !receipt.items || !receipt.final_total) {
    return res.status(400).json({ message: "Merchant name, transaction date, transaction time, items, and final total are required." });
  }

  if (!receipt.tender_type || !receipt.category_spending) {
    return res.status(400).json({ message: "Tender type or category spending are required." });
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

    // Clear the cache for this user's receipts
    // This ensures that the next time receipts are fetched, they will be retrieved from the database
    const cacheKey = `user:structs:${authenticatedUserUid}`;
    await redisClient.del(cacheKey);

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

    // Clear the cache for this user's receipts
    // This ensures that the next time receipts are fetched, they will be retrieved from the database
    const cacheKey = `user:structs:${authenticatedUserUid}`;
    await redisClient.del(cacheKey);

    res.status(200).json({ message: "Receipt deleted successfully." });
  } catch (error) {
    res.status(500).json({ message: "Failed to delete receipt.", error: error.message });
  }
};

// Get all receipts for a user (expects req.params.userUid)
exports.getMyStructs = async (req, res) => {
  const authenticatedUserUid = req.user.uid;
  const db = req.db;
  const { startDate, endDate } = req.query;

  try {
    // Gunakan cache hanya jika tidak ada filter
    const useCache = !startDate && !endDate;
    const cacheKey = `user:structs:${authenticatedUserUid}`;

    if (useCache) {
      const cachedStructs = await redisClient.get(cacheKey);
      if (cachedStructs) {
        return res.status(200).json(JSON.parse(cachedStructs));
      }
    }

    const userRef = db.collection('users').doc(authenticatedUserUid);
    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const receipts = userDoc.data().receipts || [];

    // Jika ada filter tanggal, lakukan filter manual
    let result = receipts;
    if (startDate && endDate) {
      result = receipts.filter(receipt =>
        receipt.transaction_date >= startDate &&
        receipt.transaction_date <= endDate
      );
    }

    // Simpan ke cache hanya jika tidak pakai filter
    if (useCache) {
      await redisClient.setEx(cacheKey, 3600, JSON.stringify(result));
    }

    res.status(200).json(result);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch receipts.", error: error.message });
  }
};

exports.getMyStructById = async (req, res) => {
  const authenticatedUserUid = req.user.uid;
  const receiptId = req.params.id;
  const db = req.db;

  if (!receiptId) {
    return res.status(400).json({ message: "Receipt ID is required." });
  }

  try {
    const userRef = db.collection('users').doc(authenticatedUserUid);
    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const receipts = userDoc.data().receipts || [];
    const receipt = receipts.find(r => r.id === receiptId);

    if (!receipt) {
      return res.status(404).json({ message: "Receipt not found." });
    }

    res.status(200).json(receipt);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch receipt.", error: error.message });
  }
};
