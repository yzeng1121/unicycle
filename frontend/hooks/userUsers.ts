import { useState, useEffect } from 'react'
import { supabase } from '../lib/supabaseClient'

export function useUsers() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true)
      const { data, error } = await supabase
        .from('users')
        .select('*')
      
      if (error) setError(error)
      else setUsers(data)
      setLoading(false)
    }
    
    fetchUsers()
  }, [])

  return { users, loading, error }
}