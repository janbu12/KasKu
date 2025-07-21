const express = require('express');
const { User } = require('../models/User');

exports.getDashboardData = async (req, res) => {
    const db = req.db;
    const uid = req.user.uid;
    const now = new Date();
    const thisMonth = now.getMonth();
    const thisYear = now.getFullYear();
    const today = now.getDate();

    try {
        const userDoc = await db.collection('users').doc(uid).get();
        if (!userDoc.exists) return res.status(404).json({ error: 'User not found' });

        const user = User.fromFirestore(uid, userDoc.data());
        const income = user.userProfile?.income || 0;
        const receipts = user.receipts || [];

        console.log(receipts.map(r => r.transaction_date));

        const monthReceipts = receipts.filter(r => {
            if (!r.transaction_date) return false;
            const date = r.transaction_date instanceof Date ? r.transaction_date : new Date(r.transaction_date);
            console.log(date);
            if (!date) return false;
            return date.getMonth() === thisMonth && date.getFullYear() === thisYear;
        });

        const todayReceipts = monthReceipts.filter(r => {
            const date = r.transaction_date instanceof Date ? r.transaction_date : new Date(r.transaction_date);
            return date.getDate() === today;
        });

        const yearReceipts = receipts.filter(r => {
            if (!r.transaction_date) return false;
                const date = r.transaction_date instanceof Date ? r.transaction_date : new Date(r.transaction_date);
            if (!date) return false;
            return date.getFullYear() === thisYear;
        });

        // Line Chart Data
        const daysInMonth = new Date(thisYear, thisMonth + 1, 0).getDate();

        const lineChartData = Array.from({ length: daysInMonth }, (_, day) => {
            const date = new Date(thisYear, thisMonth, day + 1);

            const dayReceipts = monthReceipts.filter(r => {
                const rDate = r.transaction_date instanceof Date ? r.transaction_date : new Date(r.transaction_date);
                return rDate.getDate() === date.getDate();
            });

            const dayTotal = dayReceipts.reduce((sum, r) => sum + r.final_total, 0);
            return {
                day: day + 1,
                income: income / daysInMonth, // evenly spread
                expense: dayTotal,
            };
        });

        // Pie Chart Data
        const categoryTotals = {};
        for (const r of monthReceipts) {
            const category = (r.category_spending || 'Lainnya').toLowerCase();
            if (!categoryTotals[category]) categoryTotals[category] = 0;
            categoryTotals[category] += r.final_total;
        }

        const overspentCategoriesMonthly = Object.entries(categoryTotals)
            .filter(([_, amount]) => amount > income)
            .map(([category, amount]) => ({
                category,
                amount,
                exceededBy: amount - income,
                percentage: income > 0 ? ((amount / income) * 100).toFixed(2) + '%' : 'N/A',
        }));

        const overspentCategoriesDaily = Object.entries(categoryTotals)
            .filter(([_, amount]) => amount > (income / daysInMonth))
            .map(([category, amount]) => ({
                category,
                amount,
                exceededBy: amount - (income / daysInMonth),
                percentage: (income / daysInMonth) > 0 ? ((amount / (income / daysInMonth)) * 100).toFixed(2) + '%' : 'N/A',
        }));

        const totalMonthSpending = monthReceipts.reduce((sum, r) => sum + r.final_total, 0);
        const totalYearSpending = yearReceipts.reduce((sum, r) => sum + r.final_total, 0);
        const totalTodaySpending = todayReceipts.reduce((sum, r) => sum + r.final_total, 0);

        const pieChartData = Object.entries(categoryTotals).map(([category, amount]) => ({
            category,
            amount,
            percentage: totalMonthSpending ? (amount / totalMonthSpending) * 100 : 0,
        }));

        return res.json({
            lineChartData,
            pieChartData,
            totalMonthSpending,
            totalYearSpending,
            totalTodaySpending,
            income,
            overspentCategoriesMonthly,
            overspentCategoriesDaily,
            monthReceipts
        });

    } catch (err) {
        console.error('Dashboard error:', err);
        res.status(500).json({ error: 'Internal server error' });
    }
};