import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import  logo  from "../assets/billsplitlogo.png";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <nav className="bg-white border-b border-slate-200 sticky top-0 z-10">
      <div className="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="font-bold text-xl text-brand-700 flex items-center gap-2">
          <span className="text-2xl">
            <img src={logo} alt="billsplit logo" className="w-6 h-6 inline-block" />
          </span> BillSplit
        </Link>
        <div className="flex items-center gap-5 text-sm font-medium text-slate-600">
          {user ? (
            <>
              <Link to="/dashboard" className="hover:text-brand-600">My Groups</Link>
              <span className="text-slate-800">{user.fullName}</span>
              <button onClick={() => { logout(); navigate('/login'); }}
                className="bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-lg">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="hover:text-brand-600">Login</Link>
              <Link to="/register"
                className="bg-brand-600 hover:bg-brand-700 text-white px-4 py-1.5 rounded-lg">
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
