// 2025-08-05
// author : Soeun

const { createClient } = require('@supabase/supabase-js');
const fetch = require('node-fetch'); // Node 전용 fetch


const supabase = createClient(
    process.env.SUPABASE_URL,
    process.env.SUPABASE_SERVICE_ROLE_KEY,
    {
        global: {
            fetch, //.pipe() 되는 Node.js ReadableStream 반환
        },
    }
);

module.exports = supabase;