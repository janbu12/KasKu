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

exports.getProfileUser = async (req, res) => {
  const { uid } = req.params;
  const authenticatedUserUid = req.user.uid;

  if (!uid) {
    return res.status(400).json({ message: "User ID (uid) is required." });
  }

  if (authenticatedUserUid !== uid) {
      return res.status(403).send({ message: "Forbidden: You can only view your own profile." });
  }

  try {
    const userDoc = await dbFirestore.collection('users').doc(uid).get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const user = User.fromFirestore(userDoc.id, userDoc.data());
    const filteredUser = {
      uid: user.uid,
      username: user.username,
      email: user.email,
      createdAt: user.createdAt,
      profileCompleted: user.profileCompleted,
      userProfile: user.userProfile ?? null
    };
    res.status(200).json(filteredUser);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch user profile.", error: error.message });
  }
};

exports.updateProfileUser = async (req, res) => {
  const { uid } = req.params;
  const authenticatedUserUid = req.user.uid;
  const { occupation, income, financialGoals, currency, ...additionalData } = req.body;

  if (!uid) {
      return res.status(400).send({ message: "User ID (uid) is required." });
  }

  if (authenticatedUserUid !== uid) {
      return res.status(403).send({ message: "Forbidden: You can only update your own profile." });
  }

  try {
      const userDocRef = dbFirestore.collection('users').doc(uid);
      const userDoc = await userDocRef.get();

      if (!userDoc.exists) {
          return res.status(404).send({ message: 'User not found.' });
      }

      if (!occupation || income === undefined || !financialGoals) {
          return res.status(400).send({ message: 'Occupation, income, and financial goals are required for profile update.' });
      }

      const newUserProfile = new UserProfile(occupation, income, financialGoals, currency, additionalData);

      await userDocRef.update({
          userProfile: newUserProfile.toFirestore(),
          profileCompleted: true
      });

      res.status(200).send({ message: 'User profile updated successfully!', updatedProfile: newUserProfile.toFirestore() });

  } catch (error) {
      console.error(`Error updating user profile for UID ${uid}:`, error);
      res.status(500).send({ message: "Failed to update user profile.", error: error.message });
  }
};