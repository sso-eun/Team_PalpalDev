// components/RevenueChart.jsx
import React from 'react';
import { Pie } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    ArcElement,
    Tooltip,
    Legend
} from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

const data = {
    labels: ['Direct', 'Social', 'Referral'],
    datasets: [
        {
            data: [55, 30, 15],
            backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc'],
            hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf'],
            hoverBorderColor: 'rgba(234, 236, 244, 1)',
        },
    ],
};

const options = {
    responsive: true,
    plugins: {
        legend: {
            position: 'bottom'
        }
    }
};

export default function RevenueChart() {
    return <Pie data={data} options={options} />;
}
