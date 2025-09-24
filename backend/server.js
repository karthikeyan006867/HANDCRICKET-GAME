const express = require('express');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3000;
const DATA_FILE = path.join(__dirname, 'students.json');

app.use(cors());
app.use(express.json());

// Helper to read students
function readStudents() {
  if (!fs.existsSync(DATA_FILE)) return [];
  const data = fs.readFileSync(DATA_FILE);
  return JSON.parse(data);
}

// Helper to write students
function writeStudents(students) {
  fs.writeFileSync(DATA_FILE, JSON.stringify(students, null, 2));
}

// Get all students
app.get('/api/students', (req, res) => {
  res.json(readStudents());
});

// Add a student
app.post('/api/students', (req, res) => {
  const { name, age, class: studentClass } = req.body;
  if (!name || !age || !studentClass) {
    return res.status(400).json({ error: 'Missing fields' });
  }
  const students = readStudents();
  const newStudent = { id: Date.now(), name, age, class: studentClass };
  students.push(newStudent);
  writeStudents(students);
  res.status(201).json(newStudent);
});

// Delete a student
app.delete('/api/students/:id', (req, res) => {
  const id = parseInt(req.params.id);
  let students = readStudents();
  students = students.filter(s => s.id !== id);
  writeStudents(students);
  res.json({ success: true });
});

// Edit a student
app.put('/api/students/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const { name, age, class: studentClass } = req.body;
  let students = readStudents();
  const idx = students.findIndex(s => s.id === id);
  if (idx === -1) return res.status(404).json({ error: 'Student not found' });
  students[idx] = { ...students[idx], name, age, class: studentClass };
  writeStudents(students);
  res.json(students[idx]);
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
