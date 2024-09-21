//Select to getClient Detail for Login
SELECT email
FROM client 
WHERE email='sowmya@gmail.com';

//Select to getClient Detail for Login
SELECT c.client_id, c.email, c.password, c.name, c.date_of_birth, c.country, c.is_admin, ci.type, ci.value
FROM client c
INNER JOIN client_identification ci
ON c.client_id = ci.client_id
WHERE email='sowmya@gmail.com';