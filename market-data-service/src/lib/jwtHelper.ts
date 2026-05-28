import jwt from 'jsonwebtoken'

const JWT_SECRET = process.env.JWT_SECRET
if (!JWT_SECRET) throw new Error('JWT_SECRET env var required')

export function verifyToken(token: string): { sub: string; email: string; isAdmin: boolean } {
  return jwt.verify(token, JWT_SECRET!) as { sub: string; email: string; isAdmin: boolean }
}
