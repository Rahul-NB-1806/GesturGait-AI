import React, { useState, useEffect, createContext, useContext } from 'react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import {
  Search, Activity, Footprints, Brain, AlertTriangle,
  TrendingUp, User, Lock, Mail,
  LogOut, LayoutDashboard, Database, ShieldCheck, Bell
} from 'lucide-react';

const API_BASE = process.env.REACT_APP_API_URL || 'https://gesturgait-ai.onrender.com';

const AuthContext = createContext(null);

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const savedUser = localStorage.getItem('gesturgait_user');
    if (savedUser) setUser(JSON.parse(savedUser));
  }, []);

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem('gesturgait_user', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('gesturgait_user');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {user ? <Dashboard /> : <LoginScreen />}
    </AuthContext.Provider>
  );
}

function LoginScreen() {
  const { login } = useContext(AuthContext);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      const data = await res.json();
      if (res.ok) {
        login(data);
      } else {
        setError(data.message || 'Login failed');
      }
    } catch (err) {
      setError(`Connection Error: ${err.message}. Check if backend is running on ${API_BASE}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-2xl shadow-xl shadow-blue-200 mb-4 animate-bounce">
            <Activity className="text-white w-8 h-8" />
          </div>
          <h1 className="text-3xl font-extrabold font-jakarta tracking-tight text-slate-900">
            GesturGait <span className="text-blue-600">AI</span>
          </h1>
          <p className="text-slate-500 mt-2 font-medium">Researcher Portal Access</p>
        </div>

        <div className="bg-white/70 backdrop-blur-xl p-8 rounded-3xl shadow-xl border border-white/50">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-12 pr-4 py-3 bg-slate-100/50 border-none rounded-2xl text-slate-900 focus:ring-2 focus:ring-blue-500 transition-all font-medium"
                  placeholder="name@university.edu"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-12 pr-4 py-3 bg-slate-100/50 border-none rounded-2xl text-slate-900 focus:ring-2 focus:ring-blue-500 transition-all font-medium"
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>

            {error && (
              <div className="bg-red-50 text-red-600 p-3 rounded-xl text-sm font-bold flex items-center gap-2">
                <AlertTriangle className="w-4 h-4" />
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold text-lg shadow-lg shadow-blue-200 hover:bg-blue-700 active:scale-[0.98] transition-all disabled:opacity-50"
            >
              {loading ? 'Authenticating...' : 'Sign In to Portal'}
            </button>
          </form>

          <div className="mt-8 pt-6 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-400 font-bold uppercase tracking-widest flex items-center justify-center gap-2">
              <ShieldCheck className="w-4 h-4" /> Secure Researcher Auth
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

function Dashboard() {
  const { user, logout } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('Patient Analysis');
  // Auto-fill patientId from the logged-in user object
  const [patientId, setPatientId] = useState(user?.user?.patientId || '');
  const [data, setData] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = React.useCallback(async () => {
    if (!patientId) return;
    setLoading(true);
    setError(null);
    try {
      const latestRes = await fetch(`${API_BASE}/score/${patientId}/latest`);
      const latestData = await latestRes.json();

      const historyRes = await fetch(`${API_BASE}/score/${patientId}/history`);
      const historyData = await historyRes.json();

      setData(latestData);
      setHistory(historyData.history?.reverse() || []);
    } catch (err) {
      setError(`Connection Error: ${err.message}. Ensure backend is running.`);
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [patientId]);

  useEffect(() => {
    if (patientId) {
      fetchData();
    }
  }, [fetchData]);

  const renderContent = () => {
    if (activeTab !== 'Patient Analysis') {
      return (
        <div className="flex flex-col items-center justify-center py-40 border-2 border-dashed border-slate-200 rounded-[3rem]">
          <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mb-6">
            <ShieldCheck className="w-8 h-8 text-blue-400" />
          </div>
          <h3 className="text-xl font-bold text-slate-800">{activeTab}</h3>
          <p className="text-slate-400 font-medium text-center mt-2">
            This module is being initialized with your clinical data.<br/>
            Please check back shortly.
          </p>
        </div>
      );
    }

    if (loading) {
      return (
        <div className="flex flex-col items-center justify-center py-40">
           <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
           <p className="mt-4 text-slate-500 font-bold">Querying Clinical Database...</p>
        </div>
      );
    }

    if (data || !loading) {
      return (
        <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <StatCard
                title="Neurological Score"
                value={data?.score || '--'}
                sub={data?.date ? `Last: ${data.date}` : 'No data available'}
                icon={<Brain className="w-6 h-6" />}
                color="blue"
              />
              <StatCard
                title="Model Confidence"
                value={data?.confidence ? `${parseFloat(data.confidence).toFixed(2)}%` : '--'}
                sub="Inference Reliability"
                icon={<ShieldCheck className="w-6 h-6" />}
                color="emerald"
              />
              <StatCard
                title="Daily Activity"
                value={data?.stepCount || '--'}
                sub="Steps Walked"
                icon={<Footprints className="w-6 h-6" />}
                color="amber"
              />
              <StatCard
                title="Clinical Status"
                value={data?.score ? (data.score > 60 ? 'ALERT' : 'STABLE') : '--'}
                sub="Deviation Check"
                icon={<Activity className="w-6 h-6" />}
                color={data?.score > 60 ? 'rose' : 'slate'}
              />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Chart Card */}
              <div className="lg:col-span-2 bg-white p-8 rounded-[2rem] border border-slate-200 shadow-sm relative overflow-hidden group">
                <div className="absolute top-0 right-0 p-8">
                   <div className="flex items-center gap-2 px-3 py-1 bg-blue-50 rounded-full">
                      <div className="w-1.5 h-1.5 bg-blue-600 rounded-full animate-pulse"></div>
                      <span className="text-[10px] font-bold text-blue-600 uppercase tracking-widest">Live Feed</span>
                   </div>
                </div>
                <h3 className="text-lg font-bold font-jakarta text-slate-900 mb-1 flex items-center gap-3">
                  <TrendingUp className="w-5 h-5 text-blue-600" />
                  Neurological Trend Analysis
                </h3>
                <p className="text-slate-400 text-sm font-medium mb-8">90-Day Longitudinal Observations</p>

                <div className="h-[320px] w-full">
                  {history.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={history}>
                        <defs>
                          <linearGradient id="colorScore" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#2563eb" stopOpacity={0.1}/>
                            <stop offset="95%" stopColor="#2563eb" stopOpacity={0}/>
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                        <XAxis dataKey="date" hide />
                        <YAxis domain={[0, 100]} stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                        <Tooltip
                          content={<CustomTooltip />}
                        />
                        <Area
                          type="monotone"
                          dataKey="score"
                          stroke="#2563eb"
                          strokeWidth={4}
                          fillOpacity={1}
                          fill="url(#colorScore)"
                          animationDuration={1500}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="w-full h-full bg-slate-50 rounded-2xl flex items-center justify-center border border-dashed border-slate-200">
                      <p className="text-slate-400 font-medium">No history data available</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Insights Sidebar */}
              <div className="space-y-6">
                <div className="bg-slate-900 p-8 rounded-[2rem] text-white shadow-xl shadow-slate-200 relative overflow-hidden">
                   <div className="relative z-10">
                      <h3 className="text-lg font-bold font-jakarta mb-4 opacity-80 uppercase tracking-widest text-[10px]">AI Interpretation</h3>
                      <p className="text-lg font-medium leading-relaxed mb-6 italic">
                        {data?.explanation ? `"${data.explanation}"` : "Waiting for analysis..."}
                      </p>
                      <div className="p-4 bg-white/10 backdrop-blur-md rounded-2xl border border-white/10">
                         <h4 className="text-xs font-bold uppercase tracking-widest opacity-60 mb-2">Recommendation</h4>
                         <p className="text-sm font-medium text-blue-200">
                           {data?.recommendation || "No recommendation available at this time."}
                         </p>
                      </div>
                   </div>
                   <Brain className="absolute -bottom-8 -right-8 w-40 h-40 opacity-5 text-white" />
                </div>

                <div className="bg-white p-8 rounded-[2rem] border border-slate-200 shadow-sm">
                   <h3 className="font-bold font-jakarta text-slate-900 mb-6 flex items-center justify-between">
                     Feature Deviations
                     <span className="text-[10px] font-bold text-slate-400">VS BASELINE</span>
                   </h3>
                   <div className="space-y-4">
                     {data?.deviations && data.deviations.length > 0 ? data.deviations.map((dev, i) => (
                       <div key={i} className="flex items-center justify-between p-3 bg-slate-50 rounded-xl">
                         <span className="text-sm font-bold text-slate-600">{dev.feature}</span>
                         <div className={`flex items-center gap-1.5 font-bold text-sm ${dev.direction === 'worse' ? 'text-rose-500' : 'text-emerald-500'}`}>
                           {dev.direction === 'worse' ? <TrendingUp className="w-3.5 h-3.5 rotate-180" /> : <TrendingUp className="w-3.5 h-3.5" />}
                           {Math.abs(dev.deltaPercent)}%
                         </div>
                       </div>
                     )) : (
                       <div className="p-3 bg-slate-50 rounded-xl text-center">
                         <span className="text-xs font-bold text-slate-400 italic">No deviations detected</span>
                       </div>
                     )}
                   </div>
                </div>
              </div>
            </div>
          </div>
      );
    }

    return (
      <div className="flex flex-col items-center justify-center py-40 border-2 border-dashed border-slate-200 rounded-[3rem]">
         <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-6">
           <Search className="w-8 h-8 text-slate-300" />
         </div>
         <p className="text-slate-400 font-bold text-lg">Enter a Patient ID to generate analysis</p>
         <p className="text-slate-300 text-sm mt-1">Example: GG-8F42K91</p>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] flex">
      {/* Sidebar */}
      <aside className="w-72 bg-white border-r border-slate-200 flex flex-col sticky top-0 h-screen z-20">
        <div className="p-8 flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-100">
            <Activity className="text-white w-6 h-6" />
          </div>
          <h1 className="text-xl font-extrabold font-jakarta tracking-tight">GesturGait <span className="text-blue-600">AI</span></h1>
        </div>

        <nav className="flex-1 px-4 space-y-2">
          <NavItem
            icon={<LayoutDashboard className="w-5 h-5" />}
            label="Patient Analysis"
            active={activeTab === 'Patient Analysis'}
            onClick={() => setActiveTab('Patient Analysis')}
          />
          <NavItem
            icon={<Database className="w-5 h-5" />}
            label="Data Repository"
            active={activeTab === 'Data Repository'}
            onClick={() => setActiveTab('Data Repository')}
          />
          <NavItem
            icon={<Brain className="w-5 h-5" />}
            label="AI Models"
            active={activeTab === 'AI Models'}
            onClick={() => setActiveTab('AI Models')}
          />
          <NavItem
            icon={<Bell className="w-5 h-5" />}
            label="Clinical Alerts"
            active={activeTab === 'Clinical Alerts'}
            onClick={() => setActiveTab('Clinical Alerts')}
          />
        </nav>

        <div className="p-4 mt-auto">
          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 mb-4">
             <div className="flex items-center gap-3 mb-2">
                <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                  <User className="w-4 h-4 text-blue-600" />
                </div>
                <div>
                   <p className="text-xs font-bold text-slate-900 truncate max-w-[140px]">{user?.user?.email}</p>
                   <p className="text-[10px] font-bold text-slate-400 uppercase">Researcher</p>
                </div>
             </div>
             <button
              onClick={logout}
              className="w-full flex items-center justify-center gap-2 py-2 text-xs font-bold text-red-500 hover:bg-red-50 rounded-lg transition-all"
             >
               <LogOut className="w-3.5 h-3.5" /> Sign Out
             </button>
          </div>
        </div>
      </aside>

      <main className="flex-1 p-8">
        <header className="flex items-center justify-between mb-10">
          <div>
            <h2 className="text-2xl font-extrabold font-jakarta text-slate-900 tracking-tight">Clinical Insights</h2>
            <p className="text-slate-500 font-medium">Monitoring longitudinal Parkinson's markers</p>
          </div>

          <div className="flex items-center gap-4">
            <div className="relative w-80">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
              <input
                type="text"
                value={patientId}
                readOnly
                className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm text-slate-500 shadow-sm transition-all font-medium cursor-not-allowed"
                placeholder="Patient ID..."
              />
            </div>
            <button onClick={fetchData} className="px-6 py-3 bg-blue-600 text-white rounded-2xl font-bold text-sm shadow-lg shadow-blue-100 hover:bg-blue-700 transition-all">
              Refresh
            </button>
          </div>
        </header>

        {renderContent()}
      </main>
    </div>
  );
}

function NavItem({ icon, label, active, badge, onClick }) {
  return (
    <div
      onClick={onClick}
      className={`flex items-center justify-between p-3.5 rounded-2xl cursor-pointer transition-all ${active ? 'bg-blue-50 text-blue-600' : 'text-slate-500 hover:bg-slate-50'}`}
    >
      <div className="flex items-center gap-3">
        {icon}
        <span className="font-bold text-sm">{label}</span>
      </div>
      {badge && (
        <span className="bg-blue-600 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-md leading-none">
          {badge}
        </span>
      )}
    </div>
  );
}

function StatCard({ title, value, sub, icon, color }) {
  const themes = {
    blue: 'bg-blue-50 text-blue-600 shadow-blue-50',
    emerald: 'bg-emerald-50 text-emerald-600 shadow-emerald-50',
    amber: 'bg-amber-50 text-amber-600 shadow-amber-50',
    rose: 'bg-rose-50 text-rose-600 shadow-rose-50',
    slate: 'bg-slate-50 text-slate-600 shadow-slate-50',
  };

  return (
    <div className="bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
      <div className={`w-12 h-12 rounded-2xl flex items-center justify-center mb-6 shadow-lg ${themes[color]}`}>
        {icon}
      </div>
      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">{title}</p>
      <div className="text-3xl font-extrabold font-jakarta text-slate-900 mb-1">{value}</div>
      <p className="text-xs font-bold text-slate-400">{sub}</p>
    </div>
  );
}

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-slate-900 p-4 rounded-2xl shadow-2xl border border-white/10 text-white animate-in zoom-in-95 duration-200">
        <p className="text-[10px] font-bold opacity-50 uppercase tracking-widest mb-1">{payload[0].payload.date}</p>
        <p className="text-xl font-extrabold font-jakarta">
          Score: <span className="text-blue-400">{payload[0].value}</span>
        </p>
      </div>
    );
  }
  return null;
};

export default App;
