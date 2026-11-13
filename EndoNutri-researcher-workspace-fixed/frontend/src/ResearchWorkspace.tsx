import {useEffect, useMemo, useState} from 'react'
import {api} from './api'
import {BookOpen, Download, FileSpreadsheet, FlaskConical, Search, ShieldCheck, Upload} from 'lucide-react'

type Mode = 'papers' | 'lab' | 'analytics'
type PaperTopic = {title:string; description:string; query:string; category:string}
type UploadResult = {datasetId:string; originalFilename:string; bytes:number; status:string}
type Stats = {count:number; mean:number; median:number; min:number; max:number; standardDeviation:number; variance:number}
type Aggregate = {symptom:string; date:string; entries:number; cohortSize:number; meanSeverity:number}
type AggregateResponse = {minimumCohort:number; aggregates:Aggregate[]; privacyNote:string}

const topics:PaperTopic[] = [
 {title:'Endometriosis and nutrition',description:'Research on dietary patterns, nutrition and endometriosis-related outcomes.',query:'endometriosis nutrition diet',category:'Nutrition'},
 {title:'Symptoms and quality of life',description:'Studies on pain, fatigue, symptom burden and patient-reported outcomes.',query:'endometriosis symptoms quality of life',category:'Patient outcomes'},
 {title:'Inflammation and biomarkers',description:'Literature on inflammatory pathways, biomarkers and endometriosis.',query:'endometriosis inflammation biomarkers',category:'Biomedical research'},
 {title:'Diagnosis and management',description:'Clinical research on diagnosis, treatment approaches and care pathways.',query:'endometriosis diagnosis management',category:'Clinical research'}
]

function parseCsv(source:string):string[][] {
 const rows:string[][]=[]
 let row:string[]=[], cell='', quoted=false
 for(let i=0;i<source.length;i++){
  const ch=source[i]
  if(ch==='"'){
   if(quoted&&source[i+1]==='"'){cell+='"';i++}else quoted=!quoted
  }else if(ch===','&&!quoted){row.push(cell.trim());cell=''}
  else if((ch==='\n'||ch==='\r')&&!quoted){
   if(ch==='\r'&&source[i+1]==='\n')i++
   row.push(cell.trim());cell=''
   if(row.some(v=>v!==''))rows.push(row)
   row=[]
  }else cell+=ch
 }
 row.push(cell.trim());if(row.some(v=>v!==''))rows.push(row)
 return rows
}
function numberValue(value:string):number|null {
 if(value.trim()==='')return null
 const n=Number(value)
 return Number.isFinite(n)?n:null
}
function saveCsv(filename:string, rows:string[][]) {
 const content=rows.map(row=>row.map(v=>`"${String(v??'').replace(/"/g,'""')}"`).join(',')).join('\r\n')
 const url=URL.createObjectURL(new Blob([content],{type:'text/csv;charset=utf-8'}))
 const a=document.createElement('a');a.href=url;a.download=filename;a.click();URL.revokeObjectURL(url)
}

export default function ResearchWorkspace({mode}:{mode:Mode}) {
 const [search,setSearch]=useState('')
 const [file,setFile]=useState<File|null>(null)
 const [rows,setRows]=useState<string[][]>([])
 const [filename,setFilename]=useState('')
 const [upload,setUpload]=useState<UploadResult|null>(null)
 const [column,setColumn]=useState('')
 const [stats,setStats]=useState<Stats|null>(null)
 const [aggregates,setAggregates]=useState<Aggregate[]>([])
 const [minimumCohort,setMinimumCohort]=useState(10)
 const [privacyNote,setPrivacyNote]=useState('')
 const [loading,setLoading]=useState(false)
 const [error,setError]=useState('')
 const [notice,setNotice]=useState('')

 const headers=rows[0]||[]
 const numericColumns=useMemo(()=>headers.map((name,index)=>({name,index})).filter(c=>rows.slice(1).some(r=>numberValue(r[c.index]||'')!==null)),[rows])
 const filteredTopics=topics.filter(t=>(`${t.title} ${t.description} ${t.category}`).toLowerCase().includes(search.toLowerCase()))

 useEffect(()=>{
  if(mode!=='analytics')return
  let active=true
  setLoading(true);setError('')
  api<AggregateResponse>('/research/consented-symptoms')
   .then(data=>{if(!active)return;setAggregates(data.aggregates||[]);setMinimumCohort(data.minimumCohort||10);setPrivacyNote(data.privacyNote||'Only consented, grouped data is shown.')})
   .catch(e=>{if(active)setError((e as Error).message)})
   .finally(()=>{if(active)setLoading(false)})
  return ()=>{active=false}
 },[mode])

 async function readFile(next:File|null) {
  setFile(next);setStats(null);setUpload(null);setNotice('');setError('')
  if(!next){setRows([]);setFilename('');return}
  if(!next.name.toLowerCase().endsWith('.csv')){setError('Please select a CSV file.');setRows([]);return}
  if(next.size>10*1024*1024){setError('The CSV must be smaller than 10 MB.');setRows([]);return}
  try{
   const parsed=parseCsv(await next.text())
   if(parsed.length<2||!parsed[0].length){setError('The CSV needs a header row and at least one data row.');setRows([]);return}
   setRows(parsed);setFilename(next.name)
   const initialIndex=parsed[0].findIndex((_,i)=>parsed.slice(1).some(r=>numberValue(r[i]||'')!==null))
   setColumn(initialIndex>=0?String(initialIndex):'')
  }catch{setError('Could not read this CSV file.')}
 }
 async function uploadDataset(){
  if(!file)return
  setLoading(true);setError('');setNotice('')
  try{
   const body=new FormData();body.append('file',file)
   const result=await api<UploadResult>('/research/datasets',{method:'POST',body})
   setUpload(result);setNotice('Dataset uploaded successfully.')
  }catch(e){setError((e as Error).message)}finally{setLoading(false)}
 }
 async function analyze(){
  if(column==='')return
  const values=rows.slice(1).map(r=>numberValue(r[Number(column)]||'')).filter((v):v is number=>v!==null)
  if(!values.length){setError('This column has no numeric values.');return}
  setLoading(true);setError('');setNotice('')
  try{
   const result=await api<Stats>('/research/statistics/descriptive',{method:'POST',body:JSON.stringify({values})})
   setStats(result);setNotice(`Analysis complete: ${result.count} numeric values.`)
  }catch(e){setError((e as Error).message)}finally{setLoading(false)}
 }
 const title=mode==='papers'?'Research papers':mode==='lab'?'Virtual lab':'Patient data'
 return <div className="research-workspace">
  <div className="page-heading"><p className="eyebrow">RESEARCH WORKSPACE</p><h1>{title}</h1><p className="muted">{mode==='papers'?'Explore research literature and open relevant indexed studies.':mode==='lab'?'Upload a CSV dataset, inspect its contents and calculate descriptive statistics.':'Explore privacy-protected symptom aggregates from patients who have consented to research use.'}</p></div>
  {error&&<div className="notice">{error}<button onClick={()=>setError('')}>Dismiss</button></div>}
  {notice&&<div className="research-success">{notice}</div>}
  {mode==='papers'&&<>
   <section className="panel">
    <div className="research-search"><Search size={18}/><input value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search topics in the research library"/></div>
    <div className="research-topic-grid">
     {filteredTopics.map(topic=><article className="research-topic" key={topic.title}><span className="stat-icon sage"><BookOpen/></span><span className="research-category">{topic.category}</span><h2>{topic.title}</h2><p className="muted">{topic.description}</p><a className="primary research-link" href={`https://pubmed.ncbi.nlm.nih.gov/?term=${encodeURIComponent(topic.query)}`} target="_blank" rel="noreferrer">Search PubMed</a></article>)}
    </div>
    {!filteredTopics.length&&<p className="muted">No matching topics. Try a broader search term.</p>}
   </section>
   <p className="research-footnote">Literature searches open PubMed, a biomedical research index. Review each paper's methods and publication details before relying on its findings.</p>
  </>}
  {mode==='lab'&&<>
   <section className="panel research-upload">
    <span className="stat-icon lilac"><FlaskConical/></span><h2>Upload a research dataset</h2>
    <p className="muted">CSV only, up to 10 MB. Your file is sent to the EndoNutri research API; do not upload identifiable patient information unless you have appropriate authorization.</p>
    <label className="file-picker"><Upload size={17}/><span>{file?file.name:'Choose a CSV file'}</span><input type="file" accept=".csv,text/csv" onChange={e=>readFile(e.target.files?.[0]||null)}/></label>
    {file&&<div className="research-actions"><button className="primary" onClick={uploadDataset} disabled={loading}><Upload size={15}/>{loading?'Uploading…':'Upload dataset'}</button>{upload&&<span className="muted">Uploaded · {upload.bytes.toLocaleString()} bytes</span>}</div>}
   </section>
   {rows.length>0&&<section className="panel">
    <div className="panel-head"><h2>Dataset preview</h2><span className="tag">{rows.length-1} rows · {headers.length} columns</span></div>
    <div className="research-table-wrap"><table className="research-table"><thead><tr>{headers.map((h,i)=><th key={i}>{h||`Column ${i+1}`}</th>)}</tr></thead><tbody>{rows.slice(1,7).map((r,i)=><tr key={i}>{headers.map((_,j)=><td key={j}>{r[j]||'—'}</td>)}</tr>)}</tbody></table></div>
    {rows.length>7&&<p className="research-footnote">Showing the first 6 data rows.</p>}
    <div className="research-analysis"><label>Numeric column<select value={column} onChange={e=>{setColumn(e.target.value);setStats(null)}}><option value="">Choose a column</option>{numericColumns.map(c=><option key={c.index} value={c.index}>{c.name||`Column ${c.index+1}`}</option>)}</select></label><button className="primary" disabled={loading||column===''} onClick={analyze}>{loading?'Analyzing…':'Run descriptive analysis'}</button></div>
   </section>}
   {stats&&<section className="panel"><div className="panel-head"><h2>Descriptive statistics</h2><button className="text-button" onClick={()=>saveCsv('endonutri-analysis.csv',[['Metric','Value'],...Object.entries(stats).map(([key,value])=>[key,String(value)])])}><Download size={14}/> Export CSV</button></div><div className="research-stat-grid">{Object.entries({Count:stats.count,Mean:stats.mean,Median:stats.median,Minimum:stats.min,Maximum:stats.max,'Std. deviation':stats.standardDeviation,Variance:stats.variance}).map(([k,v])=><div className="research-stat" key={k}><small>{k}</small><b>{Number(v).toLocaleString(undefined,{maximumFractionDigits:3})}</b></div>)}</div></section>}
  </>}
  {mode==='analytics'&&<>
   <section className="panel">
    <div className="panel-head"><div><h2>Consented symptom aggregates</h2><p className="muted">Minimum cohort threshold: {minimumCohort} distinct patients</p></div><span className="stat-icon sage"><ShieldCheck/></span></div>
    <p className="research-privacy"><ShieldCheck size={16}/>{privacyNote||'Only aggregated, consented data is displayed. No patient names, IDs or individual records are returned.'}</p>
    {loading?<p className="muted">Loading aggregated research data…</p>:aggregates.length===0?<div className="empty"><FileSpreadsheet size={28}/><b>No aggregates available yet</b><span>Data appears when enough consenting patients contribute records to meet the minimum cohort threshold.</span></div>:<>
     <div className="research-table-wrap"><table className="research-table"><thead><tr><th>Symptom</th><th>Date</th><th>Entries</th><th>Patient cohort</th><th>Mean severity</th></tr></thead><tbody>{aggregates.map((a,i)=><tr key={`${a.symptom}-${a.date}-${i}`}><td>{a.symptom}</td><td>{a.date}</td><td>{a.entries}</td><td>{a.cohortSize}</td><td>{Number(a.meanSeverity).toFixed(2)} / 10</td></tr>)}</tbody></table></div>
     <div className="research-bars"><h3>Average severity by reported symptom</h3>{Object.entries(aggregates.reduce<Record<string,{total:number;count:number}>>((acc,a)=>{const item=acc[a.symptom]||{total:0,count:0};item.total+=Number(a.meanSeverity);item.count++;acc[a.symptom]=item;return acc},{})).map(([symptom,v])=>{const mean=v.total/v.count;return <div className="research-bar-row" key={symptom}><span>{symptom}</span><div><i style={{width:`${Math.max(0,Math.min(100,mean*10))}%`}}/></div><b>{mean.toFixed(1)}</b></div>})}</div>
    </>}
   </section>
  </>}
 </div>
}
