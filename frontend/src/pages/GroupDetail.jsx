import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { expenseApi, settlementApi, groupApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#eab308','#0ea5e9','#10b981','#f43f5e','#8b5cf6','#f97316'];
const TABS = ['Expenses', 'Balances', 'Settle Up', 'History'];

export default function GroupDetail() {
  const { groupId } = useParams();
  const { user } = useAuth();
  const [tab, setTab] = useState('Expenses');
  const [expenses, setExpenses] = useState([]);
  const [balances, setBalances] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [history, setHistory] = useState([]);
  const [members, setMembers] = useState([]);
  const [form, setForm] = useState({ description: '', amount: '', splitAmongUserIds: [] });
  const [memberEmail, setMemberEmail] = useState('');

  const load = async () => {
    const [exp, bal, sugg, hist, mem] = await Promise.all([
      expenseApi.byGroup(groupId),
      expenseApi.balances(groupId),
      settlementApi.suggestions(groupId),
      settlementApi.history(groupId),
      groupApi.members(groupId),
    ]);
    setExpenses(exp.data);
    setBalances(bal.data);
    setSuggestions(sugg.data);
    setHistory(hist.data);
    setMembers(mem.data);
  };

  useEffect(() => { load(); }, [groupId]);

  const addExpense = async (e) => {
    e.preventDefault();
    await expenseApi.add({
      description: form.description,
      amount: parseFloat(form.amount),
      groupId: parseInt(groupId),
      splitAmongUserIds: form.splitAmongUserIds.length > 0 ? form.splitAmongUserIds : null,
    });
    setForm({ description: '', amount: '', splitAmongUserIds: [] });
    load();
  };

  const addMember = async (e) => {
    e.preventDefault();
    await groupApi.addMember(groupId, { email: memberEmail });
    setMemberEmail('');
    load();
  };

  const settle = async (s) => {
    await settlementApi.record({
      groupId: parseInt(groupId),
      fromUserId: s.fromUserId,
      toUserId: s.toUserId,
      amount: s.amount,
    });
    load();
  };

  const pieData = balances.map(b => ({
    name: b.userName,
    value: parseFloat(b.totalPaid),
  })).filter(d => d.value > 0);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Group #{groupId}</h1>
        <div className="flex gap-2">
          {TABS.map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`text-sm px-4 py-1.5 rounded-lg font-medium transition-colors ${
                tab === t
                  ? 'bg-brand-600 text-white'
                  : 'bg-white border border-slate-200 text-slate-600 hover:border-brand-300'
              }`}>
              {t}
            </button>
          ))}
        </div>
      </div>

      {/* Members bar */}
      <div className="bg-white border border-slate-200 rounded-xl p-4 mb-6 flex items-center gap-4 flex-wrap">
        <span className="text-sm font-medium text-slate-500">Members:</span>
        {members.map(m => (
          <span key={m.userId}
            className="bg-brand-50 text-brand-700 text-xs font-medium px-3 py-1 rounded-full">
            {m.fullName}
          </span>
        ))}
        <form onSubmit={addMember} className="flex gap-2 ml-auto">
          <input placeholder="Add by email" value={memberEmail}
            onChange={e => setMemberEmail(e.target.value)}
            className="text-sm border border-slate-300 rounded-lg px-3 py-1.5"/>
          <button type="submit"
            className="text-sm bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-lg">
            Add
          </button>
        </form>
      </div>

      {/* EXPENSES TAB */}
      {tab === 'Expenses' && (
        <div>
          <form onSubmit={addExpense}
            className="bg-white border border-slate-200 rounded-xl p-5 mb-6 flex gap-3 flex-wrap">
            <input placeholder="What was it for?" required value={form.description}
              onChange={e => setForm({...form, description: e.target.value})}
              className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm min-w-40"/>
            <input type="number" placeholder="Amount (₹)" required value={form.amount} min="0.01" step="0.01"
              onChange={e => setForm({...form, amount: e.target.value})}
              className="w-36 border border-slate-300 rounded-lg px-3 py-2 text-sm"/>
            <button type="submit"
              className="bg-brand-600 text-white text-sm px-5 py-2 rounded-lg font-medium">
              + Add Expense
            </button>
          </form>
          <div className="space-y-3">
            {expenses.length === 0
              ? <p className="text-slate-400 text-center py-8">No expenses yet.</p>
              : expenses.map(e => (
              <div key={e.id}
                className="bg-white border border-slate-200 rounded-xl p-4 flex items-start justify-between">
                <div>
                  <p className="font-medium text-slate-900">{e.description}</p>
                  <p className="text-sm text-slate-500 mt-0.5">
                    Paid by <span className="font-medium">{e.paidByName}</span>
                  </p>
                  <div className="flex gap-2 mt-2 flex-wrap">
                    {e.splits.map(s => (
                      <span key={s.userId}
                        className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
                        {s.userName}: ₹{parseFloat(s.amountOwed).toFixed(2)}
                      </span>
                    ))}
                  </div>
                </div>
                <span className="font-bold text-brand-700 text-lg">
                  ₹{parseFloat(e.amount).toFixed(2)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* BALANCES TAB */}
      {tab === 'Balances' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white border border-slate-200 rounded-xl p-5">
            <h2 className="font-semibold text-slate-900 mb-4">Who paid what</h2>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={pieData} dataKey="value" nameKey="name"
                  cx="50%" cy="50%" outerRadius={80} label>
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={v => `₹${v.toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="bg-white border border-slate-200 rounded-xl p-5">
            <h2 className="font-semibold text-slate-900 mb-4">Net balances</h2>
            <div className="space-y-3">
              {balances.map(b => {
                const net = parseFloat(b.netBalance);
                return (
                  <div key={b.userId}
                    className="flex items-center justify-between border-b border-slate-100 pb-2">
                    <span className="font-medium text-slate-700">{b.userName}</span>
                    <span className={`font-bold text-sm ${net >= 0 ? 'text-emerald-600' : 'text-red-500'}`}>
                      {net >= 0 ? `gets back ₹${net.toFixed(2)}` : `owes ₹${Math.abs(net).toFixed(2)}`}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* SETTLE UP TAB */}
      {tab === 'Settle Up' && (
        <div className="bg-white border border-slate-200 rounded-xl p-5">
          <h2 className="font-semibold text-slate-900 mb-2">Minimum transactions to settle</h2>
          <p className="text-slate-500 text-sm mb-5">
            Debt minimization algorithm — same approach as Splitwise.
          </p>
          {suggestions.length === 0 ? (
            <div className="text-center py-8 text-emerald-600 font-medium">
              ✅ All settled up! No pending debts.
            </div>
          ) : (
            <div className="space-y-3">
              {suggestions.map((s, i) => (
                <div key={i}
                  className="flex items-center justify-between bg-amber-50 border border-amber-200 rounded-xl p-4">
                  <div>
                    <span className="font-semibold text-slate-900">{s.fromUserName}</span>
                    <span className="text-slate-500 mx-2">pays</span>
                    <span className="font-semibold text-slate-900">{s.toUserName}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-bold text-brand-700 text-lg">
                      ₹{parseFloat(s.amount).toFixed(2)}
                    </span>
                    {s.fromUserId === user.id && (
                    <button onClick={() => settle(s)}
                      className="text-sm bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded-lg">
                      Mark Paid
                    </button>
                    )}         
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* HISTORY TAB */}
      {tab === 'History' && (
        <div className="bg-white border border-slate-200 rounded-xl p-5">
          <h2 className="font-semibold text-slate-900 mb-4">Settlement History</h2>
          {history.length === 0 ? (
            <p className="text-slate-400 text-center py-8">No settlements yet.</p>
          ) : (
            <div className="space-y-3">
              {history.map(s => (
                <div key={s.id}
                  className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <div className="text-sm">
                    <span className="font-medium">{s.fromUserName}</span>
                    <span className="text-slate-400 mx-2">→</span>
                    <span className="font-medium">{s.toUserName}</span>
                    <span className="text-slate-400 ml-2 text-xs">
                      {new Date(s.settledAt).toLocaleDateString()}
                    </span>
                  </div>
                  <span className="font-bold text-emerald-600">
                    ₹{parseFloat(s.amount).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
