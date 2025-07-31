// components/EarningsChart.jsx
import React from 'react';
import {
    Chart as ChartJS,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Tooltip,
    Legend
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend);

const data = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
        {
            label: 'Earnings',
            data: [12000, 19000, 3000, 5000, 20000, 30000],
            borderColor: '#4e73df',
            backgroundColor: 'rgba(78, 115, 223, 0.05)',
            tension: 0.3,
            fill: true
        }
    ]
};

const options = {
    responsive: true,
    plugins: {
        legend: { display: false },
    },
    scales: {
        y: { beginAtZero: true },
        x: { grid: { display: false } }
    }
};

export default function EarningsChart() {
    return <Line data={data} options={options} />;
}
