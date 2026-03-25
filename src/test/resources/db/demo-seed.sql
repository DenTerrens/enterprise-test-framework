delete from app_admin;
delete from managed_user;
delete from upload_audit;

insert into app_admin (username, password) values ('admin', 'secret123');

insert into managed_user (name, email, role, status) values
('Seeded User', 'seeded.user@demo.local', 'ADMIN', 'ACTIVE');
