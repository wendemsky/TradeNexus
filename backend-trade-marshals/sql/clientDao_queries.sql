//Select to verify if client email exists
SELECT email
FROM client 
WHERE email = 'sowmya@gmail.com';

//Select to getClient Detail for Login
SELECT c.client_id, c.email, c.password, c.name, c.date_of_birth, c.country, c.is_admin, ci.type, ci.value
FROM client c
INNER JOIN client_identification ci
ON c.client_id = ci.client_id
WHERE c.email= 'sowmya@gmail.com'
ORDER BY c.client_id;

//Select to get all client identification details - for verification
SELECT type, value
FROM client_identification
ORDER BY client_id;