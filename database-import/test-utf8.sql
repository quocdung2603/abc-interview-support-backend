\c questiondb;
DELETE FROM topics WHERE field_id=1;
DELETE FROM fields WHERE id=1;
INSERT INTO fields(id, name, description) VALUES (1, 'Lập trình viên', 'Test UTF-8');
SELECT id, name, description FROM fields WHERE id=1;
