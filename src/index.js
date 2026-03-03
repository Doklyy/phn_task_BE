/**
 * Backend API – khớp FE: reminder, chấm công theo tháng, điểm & bảng xếp hạng.
 * Chạy: npm install && npm run init-db && npm start
 */
import express from 'express';
import cors from 'cors';
import { reportsRouter } from './routes/reports.js';
import { attendanceRouter } from './routes/attendance.js';
import { scoringRouter } from './routes/scoring.js';
import { usersRouter } from './routes/users.js';
import { tasksRouter } from './routes/tasks.js';

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors({ origin: true, credentials: true }));
app.use(express.json());

app.use('/api/reports', reportsRouter);
app.use('/api/attendance', attendanceRouter);
app.use('/api/scoring', scoringRouter);
app.use('/api/users', usersRouter);
app.use('/api/tasks', tasksRouter);

app.get('/api/health', (req, res) => res.json({ ok: true }));

app.listen(PORT, () => {
  console.log(`Backend chạy tại http://localhost:${PORT}/api`);
});
