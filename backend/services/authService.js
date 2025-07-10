// services/authService.js
const { getAuth } = require('firebase-admin/auth'); // Gunakan firebase-admin/auth untuk createUser dan revokeRefreshTokens
const { getFirestore } = require('firebase-admin/firestore'); // Untuk interaksi dengan Firestore
const { User } = require('../models/User'); // Pastikan path ke model User benar

// Utility function to validate password
function validatePassword(password) {
    if (password.length < 6) {
        return 'Password must be at least 6 characters long.';
    }
    if (!/[A-Z]/.test(password)) {
        return 'Password must contain at least one uppercase letter.';
    }
    if (!/[a-z]/.test(password)) {
        return 'Password must contain at least one lowercase letter.';
    }
    if (!/[0-9]/.test(password)) {
        return 'Password must contain at least one number.';
    }
    if (!/[^A-Za-z0-9]/.test(password)) {
        return 'Password must contain at least one special character/symbol.';
    }
    return null;
}

/**
 * Registers a new user with Firebase Authentication and stores user data in Firestore.
 * @param {string} username
 * @param {string} email
 * @param {string} password
 * @returns {object} user data (uid, username, email)
 * @throws {Error} if registration fails
 */
async function registerUser(username, email, password, auth, db) {
    const usernameSnapshot = await db.collection('users').where('username', '==', username).get();
    if (!usernameSnapshot.empty) {
        const error = new Error('Username already in use. Please choose a different username.');
        error.code = 'auth/username-already-exists';
        throw error;
    }

    const passwordError = validatePassword(password);
    if (passwordError) {
        const error = new Error(passwordError);
        error.code = 'auth/invalid-password-format';
        throw error;
    }

    try {
        const userRecord = await auth.createUser({
            email: email,
            password: password,
            displayName: username,
        });

        const newUser = new User(
            userRecord.uid,
            username,
            email,
            new Date(),
            false
        );

        await db.collection('users').doc(newUser.uid).set(newUser.toFirestore());

        return {
            uid: newUser.uid,
            username: newUser.username,
            email: newUser.email
        };

    } catch (error) {
        if (error.code === 'auth/email-already-exists') {
            const customError = new Error('Email already in use. Please use a different email.');
            customError.code = 'auth/email-already-exists';
            throw customError;
        }
        if (error.code === 'auth/invalid-email') {
            const customError = new Error('Invalid email format.');
            customError.code = 'auth/invalid-email';
            throw customError;
        }
        throw error;
    }
}

/**
 * 
 * @param {string} email
 * @param {string} password
 * @param {object} app_client_sdk
 * @returns {object}
 * @throws {Error}
 */
async function loginUser(email, password, app_client_sdk) {
    try {
        const authClient = require('firebase/auth');
        const userCredential = await authClient.signInWithEmailAndPassword(app_client_sdk, email, password);
        const user = userCredential.user;
        const token = await user.getIdToken();
        return {
            uid: user.uid,
            email: user.email,
            token
        };
    } catch (error) {
        if (error.code === 'auth/user-not-found' || error.code === 'auth/wrong-password' || error.code === 'auth/invalid-credential') {
            const customError = new Error('Invalid email or password.');
            customError.code = 'auth/invalid-credentials';
            throw customError;
        }
        if (error.code === 'auth/invalid-email') {
            const customError = new Error('Invalid email format.');
            customError.code = 'auth/invalid-email';
            throw customError;
        }
        throw error;
    }
}

/**
 *
 * @param {string} uid
 * @throws {Error}
 */
async function logoutUser(uid) {
    try {
        const auth = getAuth();
        await auth.revokeRefreshTokens(uid);
    } catch (error) {
        throw error;
    }
}

module.exports = {
    registerUser,
    loginUser,
    logoutUser,
};