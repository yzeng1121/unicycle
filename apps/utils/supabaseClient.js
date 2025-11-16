import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://mkzfokxseovpuycbbmts.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1remZva3hzZW92cHV5Y2JibXRzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA3Mjc4NDgsImV4cCI6MjA3NjMwMzg0OH0.DMvshczvk26Z6NfouPPndZRhha9NVAcLpVoOlWvsgvs' // Get from Supabase → Settings → API

export const supabase = createClient(supabaseUrl, supabaseAnonKey)