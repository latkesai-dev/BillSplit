import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { groupApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name: '', description: '' });
  const [loading, setLoading] = useState(true);

  const load = () => groupApi.myGroups()
    .then(res => setGroups(res.data))
    .finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const createGroup = async (e) => {
    e.preventDefault();
    await groupApi.create(form);
    setForm({ name: '', description: '' });
    setShowCreate(false);
    load();
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">My Groups</h1>
          <p className="text-slate-500 mt-1">Welcome back, {user?.fullName}</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)}
          className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-5 py-2.5 rounded-lg">
          + New Group
        </button>
      </div>

      {showCreate && (
        <form onSubmit={createGroup}
          className="bg-white border border-slate-200 rounded-xl p-5 mb-6 flex gap-3">
          <input placeholder="Group name (e.g. Goa Trip)" required value={form.name}
            onChange={e => setForm({...form, name: e.target.value})}
            className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm"/>
          <input placeholder="Description (optional)" value={form.description}
            onChange={e => setForm({...form, description: e.target.value})}
            className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm"/>
          <button type="submit"
            className="bg-brand-600 text-white text-sm px-4 py-2 rounded-lg">
            Create
          </button>
        </form>
      )}

      {loading ? (
        <p className="text-slate-400">Loading groups...</p>
      ) : groups.length === 0 ? (
        <div className="bg-white border border-slate-200 rounded-xl p-12 text-center text-slate-400">
          <p className="text-4xl mb-3">🧾</p>
          <p className="font-medium">No groups yet.</p>
          <p className="text-sm mt-1">Create one to start splitting bills.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {groups.map(g => (
            <div key={g.id} onClick={() => navigate(`/group/${g.id}`)}
              className="bg-white border border-slate-200 rounded-xl p-5 cursor-pointer hover:shadow-md hover:border-brand-300 transition-all">
              <h3 className="font-semibold text-slate-900 text-lg mb-1">{g.name}</h3>
              <p className="text-slate-500 text-sm mb-4">{g.description || 'No description'}</p>
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>👥 {g.memberCount} members</span>
                <span>by {g.createdByName}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
