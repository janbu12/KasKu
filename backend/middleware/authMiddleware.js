const { auth } = require('../config/firebase');

const protect = async (req, res, next) => {
    let idToken;
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer ')) {
        idToken = req.headers.authorization.split(' ')[1];
    }

    if (!idToken) {
        return res.status(401).send({ message: 'Unauthorized: No token provided or invalid format.' });
    }

    try {
        const decodedToken = await auth.verifyIdToken(idToken, true);
        req.user = decodedToken;
        next();
    } catch (error) {
        console.error('Error verifying Firebase ID token:', error);
        if (error.code === 'auth/id-token-revoked') {
            return res.status(401).send({ message: 'Unauthorized: Token has been revoked. Please log in again.' });
        }
        if (error.code === 'auth/id-token-expired') {
            return res.status(401).send({ message: 'Unauthorized: Token expired.' });
        }
        if (error.code === 'auth/invalid-id-token') {
            return res.status(401).send({ message: 'Unauthorized: Invalid token.' });
        }
        return res.status(403).send({ message: 'Forbidden: Access denied.' });
    }
};

module.exports = { protect };