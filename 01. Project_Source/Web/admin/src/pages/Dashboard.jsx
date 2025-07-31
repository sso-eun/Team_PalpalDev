import React from 'react';
import Layout from '../components/Layout';
/* charts */
import EarningsChart from '../components/EarningsChart';
import RevenueChart from '../components/RevenueChart';
/* images * */
import postImg from '../assets/img/undraw_posting_photo.svg';

function Dashboard() {
    return (
        <Layout>
            <div className="container-fluid">
                {/* Page Heading */}
                <div className="d-sm-flex align-items-center justify-content-between mb-4">
                    <h1 className="h3 mb-0 text-gray-800">Dashboard</h1>
                    <a href="#" className="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm">
                        <i className="fas fa-download fa-sm text-white-50"></i> Generate Report
                    </a>
                </div>

                {/* Content Row */}
                <div className="row">
                    {/* Earnings (Monthly) Card Example */}
                    <div className="col-xl-3 col-md-6 mb-4">
                        <div className="card border-left-primary shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                            Earnings (Monthly)
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold text-gray-800">
                                            $40,000
                                        </div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-calendar fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Earnings (Annual) Card */}
                    <div className="col-xl-3 col-md-6 mb-4">
                        <div className="card border-left-success shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                            Earnings (Annual)
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold text-gray-800">$215,000</div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-dollar-sign fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Tasks Card */}
                    <div className="col-xl-3 col-md-6 mb-4">
                        <div className="card border-left-info shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-info text-uppercase mb-1">Tasks</div>
                                        <div className="row no-gutters align-items-center">
                                            <div className="col-auto">
                                                <div className="h5 mb-0 mr-3 font-weight-bold text-gray-800">50%</div>
                                            </div>
                                            <div className="col">
                                                <div className="progress progress-sm mr-2">
                                                    <div className="progress-bar bg-info" role="progressbar" style={{ width: '50%' }}
                                                         aria-valuenow="50" aria-valuemin="0" aria-valuemax="100"></div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-clipboard-list fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Pending Requests Card */}
                    <div className="col-xl-3 col-md-6 mb-4">
                        <div className="card border-left-warning shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                            Pending Requests
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold text-gray-800">18</div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-comments fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Charts Row */}
                <div className="row">
                    {/* Area Chart */}
                    <div className="col-xl-8 col-lg-7">
                        <div className="card shadow mb-4">
                            <div className="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                                <h6 className="m-0 font-weight-bold text-primary">Earnings Overview</h6>
                                <div className="dropdown no-arrow">
                                    <a className="dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
                                       data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <i className="fas fa-ellipsis-v fa-sm fa-fw text-gray-400"></i>
                                    </a>
                                    <div className="dropdown-menu dropdown-menu-right shadow animated--fade-in"
                                         aria-labelledby="dropdownMenuLink">
                                        <div className="dropdown-header">Dropdown Header:</div>
                                        <a className="dropdown-item" href="#">Action</a>
                                        <a className="dropdown-item" href="#">Another action</a>
                                        <div className="dropdown-divider"></div>
                                        <a className="dropdown-item" href="#">Something else here</a>
                                    </div>
                                </div>
                            </div>
                            <div className="card-body">
                                <div className="chart-area">
                                    <EarningsChart />
                                    <canvas id="myAreaChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Pie Chart */}
                    <div className="col-xl-4 col-lg-5">
                        <div className="card shadow mb-4">
                            <div className="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                                <h6 className="m-0 font-weight-bold text-primary">Revenue Sources</h6>
                                <div className="dropdown no-arrow">
                                    <a className="dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
                                       data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <i className="fas fa-ellipsis-v fa-sm fa-fw text-gray-400"></i>
                                    </a>
                                    <div className="dropdown-menu dropdown-menu-right shadow animated--fade-in"
                                         aria-labelledby="dropdownMenuLink">
                                        <div className="dropdown-header">Dropdown Header:</div>
                                        <a className="dropdown-item" href="#">Action</a>
                                        <a className="dropdown-item" href="#">Another action</a>
                                        <div className="dropdown-divider"></div>
                                        <a className="dropdown-item" href="#">Something else here</a>
                                    </div>
                                </div>
                            </div>
                            <div className="card-body">
                                <div className="chart-pie pt-4 pb-2">
                                    <RevenueChart />
                                    <canvas id="myPieChart"> </canvas>
                                </div>
                                <div className="mt-4 text-center small">
                                    <span className="mr-2"><i className="fas fa-circle text-primary"></i> Direct</span>
                                    <span className="mr-2"><i className="fas fa-circle text-success"></i> Social</span>
                                    <span className="mr-2"><i className="fas fa-circle text-info"></i> Referral</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Projects and Illustrations */}
                <div className="row">
                    <div className="col-lg-6 mb-4">
                        {/* Projects */}
                        <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Projects</h6>
                            </div>
                            <div className="card-body">
                                {[
                                    { label: "Server Migration", percent: 20, color: "bg-danger" },
                                    { label: "Sales Tracking", percent: 40, color: "bg-warning" },
                                    { label: "Customer Database", percent: 60, color: "" },
                                    { label: "Payout Details", percent: 80, color: "bg-info" },
                                    { label: "Account Setup", percent: 100, color: "bg-success" },
                                ].map((item, i) => (
                                    <div key={i}>
                                        <h4 className="small font-weight-bold">{item.label}
                                            <span className="float-right">
                      {item.percent === 100 ? "Complete!" : `${item.percent}%`}
                    </span>
                                        </h4>
                                        <div className="progress mb-4">
                                            <div className={`progress-bar ${item.color}`} role="progressbar"
                                                 style={{ width: `${item.percent}%` }}
                                                 aria-valuenow={item.percent} aria-valuemin="0" aria-valuemax="100"></div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Color System */}
                        <div className="row">
                            {[
                                { label: "Primary", code: "#4e73df", bg: "bg-primary", text: "text-white" },
                                { label: "Success", code: "#1cc88a", bg: "bg-success", text: "text-white" },
                                { label: "Info", code: "#36b9cc", bg: "bg-info", text: "text-white" },
                                { label: "Warning", code: "#f6c23e", bg: "bg-warning", text: "text-white" },
                                { label: "Danger", code: "#e74a3b", bg: "bg-danger", text: "text-white" },
                                { label: "Secondary", code: "#858796", bg: "bg-secondary", text: "text-white" },
                                { label: "Light", code: "#f8f9fc", bg: "bg-light", text: "text-black" },
                                { label: "Dark", code: "#5a5c69", bg: "bg-dark", text: "text-white" },
                            ].map((item, i) => (
                                <div className="col-lg-6 mb-4" key={i}>
                                    <div className={`card shadow ${item.bg} ${item.text}`}>
                                        <div className="card-body">
                                            {item.label}
                                            <div className={`${item.text}-50 small`}>{item.code}</div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="col-lg-6 mb-4">
                        {/* Illustrations */}
                        <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Illustrations</h6>
                            </div>
                            <div className="card-body">
                                <div className="text-center">
                                    <img className="img-fluid px-3 px-sm-4 mt-3 mb-4"
                                         style={{ width: '25rem' }}
                                         src={postImg}
                                         alt="..." />
                                </div>
                                <p>Add some quality, svg illustrations to your project courtesy of <a
                                    target="_blank" rel="nofollow" href="https://undraw.co/">unDraw</a>, a
                                    constantly updated collection of beautiful svg images that you can use
                                    completely free and without attribution!</p>
                                <a target="_blank" rel="nofollow" href="https://undraw.co/">Browse Illustrations on
                                    unDraw →</a>
                            </div>
                        </div>

                        {/* Development Approach */}
                        <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Development Approach</h6>
                            </div>
                            <div className="card-body">
                                <p>SB Admin 2 makes extensive use of Bootstrap 4 utility classes in order to reduce
                                    CSS bloat and poor page performance. Custom CSS classes are used to create
                                    custom components and custom utility classes.</p>
                                <p className="mb-0">Before working with this theme, you should become familiar with the
                                    Bootstrap framework, especially the utility classes.</p>
                            </div>
                        </div>
                    </div>









                </div>
            </div>
        </Layout>
    );
}
export default Dashboard;
