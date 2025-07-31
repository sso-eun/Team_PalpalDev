import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import MemberList from './pages/MemberList.jsx';
import AuthList from './pages/AuthList.jsx';
//css
import './assets/css/sb-admin-2.min.css';
import './assets/css/sso_custom.css';
import './assets/vendor/fontawesome-free/css/all.min.css';


function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/memberlist" element={<MemberList />} />
                <Route path="/authlist" element={<AuthList />} />
            </Routes>
        </Router>
    );
}

export default App;
