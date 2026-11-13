const BASE = (import.meta.env.VITE_API_BASE_URL || 'https://endonutri.onrender.com/api').replace(/\/$/, '')
export function token(){return localStorage.getItem('endo_token')}
export async function api<T>(path:string, init:RequestInit={}):Promise<T>{
 const headers = new Headers(init.headers)
 if(!(init.body instanceof FormData)) headers.set('Content-Type','application/json')
 if(token()) headers.set('Authorization',`Bearer ${token()}`)
 const response=await fetch(`${BASE}${path}`,{...init,headers})
 if(!response.ok){let msg=`Request failed (${response.status})`;try{const body=await response.json();msg=body.error||body.message||msg}catch{};throw new Error(msg)}
 if(response.status===204)return undefined as T
 return response.json()
}
