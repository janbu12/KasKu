const { dbFirestore } = require("../config/firebase");
const { User, UserProfile } = require("../models/User");

// Get all users
exports.getAllUser = async (req, res) => {
  try {
    const usersSnapshot = await dbFirestore.collection('users').get();
    const users = [];
    usersSnapshot.forEach(doc => {
      users.push(User.fromFirestore(doc.id, doc.data()));
    });
    res.status(200).json(users);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch users.", error: error.message });
  }
};

// Get profile of a user by UID (expects req.params.uid)
exports.getProfileUser = async (req, res) => {
  const { uid } = req.params;
  if (!uid) {
    return res.status(400).json({ message: "User ID (uid) is required." });
  }
  try {
    const userDoc = await dbFirestore.collection('users').doc(uid).get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const user = User.fromFirestore(userDoc.id, userDoc.data());
    res.status(200).json(user);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch user profile.", error: error.message });
  }
};

// Update profile of a user by UID (expects req.params.uid and req.body.userProfile)
exports.updateProfileUser = async (req, res) => {
  const { uid } = req.params;
  const { userProfile } = req.body;
  if (!uid || !userProfile) {
    return res.status(400).json({ message: "User ID (uid) and userProfile data are required." });
  }

  // Validasi
  const requiredFields = ['occupation', 'income', 'financialGoals'];
  const missingFields = requiredFields.filter(field => !userProfile[field]);
  if (missingFields.length > 0) {
    return res.status(400).json({
      message: `Field(s) required: ${missingFields.join(', ')}`
    });
  }

  try {
    // Validate and construct UserProfile
    const updatedProfile = new UserProfile(
      userProfile.occupation,
      userProfile.income,
      userProfile.financialGoals,
      userProfile.currency || 'IDR',
      userProfile.additionalData || {}
    );
    // Update Firestore
    await dbFirestore.collection('users').doc(uid).update({
      userProfile: updatedProfile.toFirestore(),
      profileCompleted: true
    });
    res.status(200).json({ message: "User profile updated successfully." });
  } catch (error) {
    res.status(500).json({ message: "Failed to update user profile.", error: error.message });
  }
};