import React, { useState } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { 
  FileCode, 
  Layers, 
  Scissors, 
  Download, 
  RotateCw, 
  ArrowUpDown, 
  Trash2, 
  Plus, 
  FileText,
  AlertCircle,
  CheckCircle2,
  UserCheck
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function SmartTools() {
  const [activeTab, setActiveTab] = useState('pdf-tools');
  const [activeTool, setActiveTool] = useState('merge');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  // File States
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [singleFile, setSingleFile] = useState(null);

  // Tool specific configurations
  const [splitMode, setSplitMode] = useState('EVERY_PAGE');
  const [ranges, setRanges] = useState('');
  const [pagesToExtract, setPagesToExtract] = useState('');
  const [rotationDegree, setRotationDegree] = useState(90);
  const [pageOrder, setPageOrder] = useState('');

  // Image to PDF options
  const [imgPageSize, setImgPageSize] = useState('A4');
  const [imgOrientation, setImgOrientation] = useState('AUTO');
  const [imgFitMode, setImgFitMode] = useState('FIT');
  const [imgMargins, setImgMargins] = useState('NONE');

  // Status & Feedback States
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [parsedResume, setParsedResume] = useState(null);

  const resetStates = () => {
    setSelectedFiles([]);
    setSingleFile(null);
    setError('');
    setSuccessMsg('');
    setParsedResume(null);
  };

  const handleMultipleFilesChange = (e) => {
    setError('');
    setSuccessMsg('');
    const files = Array.from(e.target.files);
    setSelectedFiles((prev) => [...prev, ...files]);
  };

  const handleSingleFileChange = (e) => {
    setError('');
    setSuccessMsg('');
    const file = e.target.files[0];
    if (file) {
      setSingleFile(file);
    }
  };

  const removeSelectedFile = (index) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const downloadBlob = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  // Perform Merge PDF
  const handleMergePdf = async () => {
    if (selectedFiles.length < 2) {
      setError('Please select at least 2 PDF files to merge.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      selectedFiles.forEach((file) => formData.append('files', file));
      const blob = await api.mergePdfs(formData);
      downloadBlob(blob, 'merged.pdf');
      setSuccessMsg('PDF documents merged successfully!');
    } catch (err) {
      setError(err.message || 'Failed to merge PDF documents.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Split PDF
  const handleSplitPdf = async () => {
    if (!singleFile) {
      setError('Please select a PDF file to split.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      formData.append('splitMode', splitMode);
      if (splitMode === 'PAGE_RANGES') {
        formData.append('ranges', ranges);
      }
      const blob = await api.splitPdf(formData);
      downloadBlob(blob, 'split-pages.zip');
      setSuccessMsg('PDF split successfully!');
    } catch (err) {
      setError(err.message || 'Failed to split PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Extract Pages
  const handleExtractPages = async () => {
    if (!singleFile) {
      setError('Please select a PDF file.');
      return;
    }
    if (!pagesToExtract.trim()) {
      setError('Please enter page numbers to extract.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const pagesArray = pagesToExtract.split(',').map(p => Number(p.trim())).filter(p => !isNaN(p));
      const formData = new FormData();
      formData.append('file', singleFile);
      pagesArray.forEach(p => formData.append('pages', p));
      const blob = await api.extractPages(formData);
      downloadBlob(blob, 'extracted.pdf');
      setSuccessMsg('Pages extracted successfully!');
    } catch (err) {
      setError(err.message || 'Failed to extract pages.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Rotate PDF
  const handleRotatePdf = async () => {
    if (!singleFile) {
      setError('Please select a PDF file.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      formData.append('rotation', rotationDegree);
      const blob = await api.rotatePdf(formData);
      downloadBlob(blob, 'rotated.pdf');
      setSuccessMsg('PDF rotated successfully!');
    } catch (err) {
      setError(err.message || 'Failed to rotate PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Reorder PDF
  const handleReorderPdf = async () => {
    if (!singleFile) {
      setError('Please select a PDF file.');
      return;
    }
    if (!pageOrder.trim()) {
      setError('Please enter the page order.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const orderArray = pageOrder.split(',').map(p => Number(p.trim())).filter(p => !isNaN(p));
      const formData = new FormData();
      formData.append('file', singleFile);
      orderArray.forEach(p => formData.append('pageOrder', p));
      const blob = await api.reorderPdf(formData);
      downloadBlob(blob, 'reordered.pdf');
      setSuccessMsg('PDF pages reordered successfully!');
    } catch (err) {
      setError(err.message || 'Failed to reorder pages.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Word to PDF
  const handleWordToPdf = async () => {
    if (!singleFile) {
      setError('Please select a Word document.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      const blob = await api.wordToPdf(formData);
      downloadBlob(blob, 'converted.pdf');
      setSuccessMsg('Word document converted to PDF successfully!');
    } catch (err) {
      setError(err.message || 'Failed to convert Word document to PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform PowerPoint to PDF
  const handlePowerPointToPdf = async () => {
    if (!singleFile) {
      setError('Please select a PowerPoint presentation.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      const blob = await api.powerPointToPdf(formData);
      downloadBlob(blob, 'presentation.pdf');
      setSuccessMsg('PowerPoint presentation converted to PDF successfully!');
    } catch (err) {
      setError(err.message || 'Failed to convert PowerPoint presentation to PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Excel to PDF
  const handleExcelToPdf = async () => {
    if (!singleFile) {
      setError('Please select an Excel workbook.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      const blob = await api.excelToPdf(formData);
      downloadBlob(blob, 'spreadsheet.pdf');
      setSuccessMsg('Excel workbook converted to PDF successfully!');
    } catch (err) {
      setError(err.message || 'Failed to convert Excel workbook to PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform Image to PDF
  const handleImagesToPdf = async () => {
    if (selectedFiles.length === 0) {
      setError('Please select at least one image file.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      selectedFiles.forEach((file) => {
        formData.append('files', file);
      });
      selectedFiles.forEach((_, idx) => {
        formData.append('order', idx);
      });
      formData.append('pageSize', imgPageSize);
      formData.append('orientation', imgOrientation);
      formData.append('fitMode', imgFitMode);
      formData.append('margins', imgMargins);

      const blob = await api.imagesToPdf(formData);
      downloadBlob(blob, 'images-to-pdf.pdf');
      setSuccessMsg('Images converted to PDF successfully!');
    } catch (err) {
      setError(err.message || 'Failed to convert images to PDF.');
    } finally {
      setLoading(false);
    }
  };

  // Perform PDF to Word
  const handlePdfToWord = async () => {
    if (!singleFile) {
      setError('Please select a PDF document.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      const blob = await api.pdfToWord(formData);
      
      let outName = 'converted-document.docx';
      if (singleFile.name && singleFile.name.toLowerCase().endsWith('.pdf')) {
        outName = singleFile.name.substring(0, singleFile.name.length - 4) + '.docx';
      }
      downloadBlob(blob, outName);
      setSuccessMsg('PDF converted to Word (DOCX) successfully!');
    } catch (err) {
      setError(err.message || 'Failed to convert PDF to Word.');
    } finally {
      setLoading(false);
    }
  };

  // Parse Resume
  const handleResumeParse = async () => {
    if (!singleFile) {
      setError('Please select a PDF or DOCX resume.');
      return;
    }
    setLoading(true);
    setError('');
    setParsedResume(null);
    try {
      const formData = new FormData();
      formData.append('file', singleFile);
      formData.append('includeRawText', 'false');

      const res = await api.resumeParse(formData);
      if (res && res.success) {
        setParsedResume(res.data);
        setSuccessMsg('Resume parsed successfully!');
      } else {
        throw new Error('Invalid parser response.');
      }
    } catch (err) {
      setError(err.message || 'Failed to parse resume.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-gray-100 flex flex-col">
      <Navbar
        toggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)}
        isSidebarOpen={isSidebarOpen}
        onOpenSOS={() => setIsSOSOpen(true)}
      />

      <div className="flex flex-1">
        <Sidebar
          isOpen={isSidebarOpen}
          activeTab={activeTab}
          setActiveTab={(tab) => {
            setActiveTab(tab);
            setIsSidebarOpen(false);
          }}
        />

        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto overflow-y-auto w-full">
          {/* Header Banner */}
          <div className="glass-panel rounded-3xl p-6 mb-8 border border-white/10 relative overflow-hidden bg-gradient-to-r from-indigo-900/40 via-purple-900/30 to-slate-900">
            <span className="text-xs font-bold text-indigo-400 uppercase tracking-widest">SmartTools</span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-1">
              Document Utilities & PDF Tools 🛠️
            </h1>
            <p className="text-xs text-gray-300 mt-1">Secure, client-oriented document conversion, merging, splitting, and PDF page modifications.</p>
          </div>

          {/* Sub Navigation Tabs */}
          <div className="flex border-b border-white/10 mb-8 space-x-6 overflow-x-auto pb-2">
            {[
              { id: 'merge', label: 'Merge PDF', icon: Layers },
              { id: 'split', label: 'Split PDF', icon: Scissors },
              { id: 'extract', label: 'Extract Pages', icon: FileCode },
              { id: 'rotate', label: 'Rotate Pages', icon: RotateCw },
              { id: 'reorder', label: 'Reorder Pages', icon: ArrowUpDown },
              { id: 'word-to-pdf', label: 'Word to PDF', icon: FileText },
              { id: 'powerpoint-to-pdf', label: 'PowerPoint to PDF', icon: FileText },
              { id: 'excel-to-pdf', label: 'Excel to PDF', icon: FileText },
              { id: 'image-to-pdf', label: 'Images to PDF', icon: FileText },
              { id: 'pdf-to-word', label: 'PDF to Word', icon: FileText },
              { id: 'resume-parser', label: 'Resume Parser', icon: UserCheck },
            ].map((t) => {
              const Icon = t.icon;
              return (
                <button
                  key={t.id}
                  onClick={() => {
                    setActiveTool(t.id);
                    resetStates();
                  }}
                  className={`flex items-center space-x-2 pb-2 text-xs font-bold transition-all border-b-2 ${
                    activeTool === t.id
                      ? 'border-indigo-500 text-white'
                      : 'border-transparent text-gray-400 hover:text-white'
                  }`}
                >
                  <Icon size={14} />
                  <span>{t.label}</span>
                </button>
              );
            })}
          </div>

          {/* Status Banners */}
          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center space-x-2">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {successMsg && (
            <div className="mb-6 p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center space-x-2">
              <CheckCircle2 size={16} />
              <span>{successMsg}</span>
            </div>
          )}

          {/* Tool Container Panels */}
          <div className="glass-panel rounded-3xl p-6 border border-white/10">

            {/* TOOL 1: MERGE PDF */}
            {activeTool === 'merge' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Merge Multiple PDFs</h3>
                
                {/* File Drop Area */}
                <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                  <input
                    type="file"
                    multiple
                    accept=".pdf"
                    onChange={handleMultipleFilesChange}
                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                  />
                  <div className="flex flex-col items-center space-y-2">
                    <Plus className="w-8 h-8 text-gray-400" />
                    <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF files</span>
                    <span className="text-[10px] text-gray-500">Only .pdf format accepted (Max 20MB per file)</span>
                  </div>
                </div>

                {/* Selected Files List */}
                {selectedFiles.length > 0 && (
                  <div className="space-y-2">
                    <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Queue:</h4>
                    <div className="space-y-2 max-h-60 overflow-y-auto">
                      {selectedFiles.map((file, idx) => (
                        <div key={idx} className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                          <div className="flex items-center space-x-3">
                            <FileText size={16} className="text-indigo-400" />
                            <div className="text-xs text-gray-200 truncate max-w-xs">{file.name}</div>
                            <span className="text-[10px] text-gray-500">({(file.size / (1024 * 1024)).toFixed(2)} MB)</span>
                          </div>
                          <button onClick={() => removeSelectedFile(idx)} className="text-gray-400 hover:text-red-400 transition-colors">
                            <Trash2 size={14} />
                          </button>
                        </div>
                      ))}
                    </div>

                    <button
                      onClick={handleMergePdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2 mt-4"
                    >
                      <Download size={14} />
                      <span>{loading ? 'Processing Documents...' : 'MERGE PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 2: SPLIT PDF */}
            {activeTool === 'split' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Split PDF Document</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF file</span>
                      <span className="text-[10px] text-gray-500">Only .pdf format accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">Split Mode</label>
                      <div className="grid grid-cols-2 gap-4">
                        <button
                          onClick={() => setSplitMode('EVERY_PAGE')}
                          className={`py-2 text-[11px] font-bold rounded-xl border transition-all ${
                            splitMode === 'EVERY_PAGE'
                              ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white border-indigo-400'
                              : 'glass-card text-gray-400 border-white/10 hover:text-white'
                          }`}
                        >
                          Every Page
                        </button>
                        <button
                          onClick={() => setSplitMode('PAGE_RANGES')}
                          className={`py-2 text-[11px] font-bold rounded-xl border transition-all ${
                            splitMode === 'PAGE_RANGES'
                              ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white border-indigo-400'
                              : 'glass-card text-gray-400 border-white/10 hover:text-white'
                          }`}
                        >
                          Page Ranges
                        </button>
                      </div>
                    </div>

                    {splitMode === 'PAGE_RANGES' && (
                      <div>
                        <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">Page Ranges</label>
                        <input
                          type="text"
                          value={ranges}
                          onChange={(e) => setRanges(e.target.value)}
                          placeholder="e.g. 1-3,5,8-10"
                          className="w-full glass-input rounded-xl px-4 py-2 text-xs"
                        />
                      </div>
                    )}

                    <button
                      onClick={handleSplitPdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <Scissors size={14} />
                      <span>{loading ? 'Processing Document...' : 'SPLIT PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 3: EXTRACT PAGES */}
            {activeTool === 'extract' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Extract Selected Pages</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF file</span>
                      <span className="text-[10px] text-gray-500">Only .pdf format accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">Enter Pages to Extract</label>
                      <input
                        type="text"
                        value={pagesToExtract}
                        onChange={(e) => setPagesToExtract(e.target.value)}
                        placeholder="e.g. 1,3,5"
                        className="w-full glass-input rounded-xl px-4 py-2 text-xs"
                      />
                    </div>

                    <button
                      onClick={handleExtractPages}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <FileCode size={14} />
                      <span>{loading ? 'Extracting Pages...' : 'EXTRACT PAGES'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 4: ROTATE PAGES */}
            {activeTool === 'rotate' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Rotate PDF Document</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF file</span>
                      <span className="text-[10px] text-gray-500">Only .pdf format accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">Rotation Angle</label>
                      <div className="grid grid-cols-3 gap-4">
                        {[90, 180, 270].map((deg) => (
                          <button
                            key={deg}
                            onClick={() => setRotationDegree(deg)}
                            className={`py-2 text-[11px] font-bold rounded-xl border transition-all ${
                              rotationDegree === deg
                                ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white border-indigo-400'
                                : 'glass-card text-gray-400 border-white/10 hover:text-white'
                            }`}
                          >
                            {deg}°
                          </button>
                        ))}
                      </div>
                    </div>

                    <button
                      onClick={handleRotatePdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <RotateCw size={14} />
                      <span>{loading ? 'Rotating PDF...' : 'ROTATE PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 5: REORDER PAGES */}
            {activeTool === 'reorder' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Reorder PDF Pages</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF file</span>
                      <span className="text-[10px] text-gray-500">Only .pdf format accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">Enter New Page Sequence</label>
                      <input
                        type="text"
                        value={pageOrder}
                        onChange={(e) => setPageOrder(e.target.value)}
                        placeholder="e.g. 5,1,3,2,4 (supply all pages exactly once)"
                        className="w-full glass-input rounded-xl px-4 py-2 text-xs"
                      />
                    </div>

                    <button
                      onClick={handleReorderPdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <ArrowUpDown size={14} />
                      <span>{loading ? 'Reordering Pages...' : 'REORDER PAGES'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 6: WORD TO PDF */}
            {activeTool === 'word-to-pdf' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Convert Word Document to PDF</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".doc,.docx"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add Word file</span>
                      <span className="text-[10px] text-gray-500">Only .doc and .docx formats accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <button
                      onClick={handleWordToPdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <Download size={14} />
                      <span>{loading ? 'Converting Document...' : 'CONVERT TO PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 7: POWERPOINT TO PDF */}
            {activeTool === 'powerpoint-to-pdf' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Convert PowerPoint Presentation to PDF</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".ppt,.pptx"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PowerPoint presentation</span>
                      <span className="text-[10px] text-gray-500">Only .ppt and .pptx formats accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <button
                      onClick={handlePowerPointToPdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <Download size={14} />
                      <span>{loading ? 'Converting Presentation...' : 'CONVERT TO PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 8: EXCEL TO PDF */}
            {activeTool === 'excel-to-pdf' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Convert Excel Workbook to PDF</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".xls,.xlsx"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add Excel workbook</span>
                      <span className="text-[10px] text-gray-500">Only .xls and .xlsx formats accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <button
                      onClick={handleExcelToPdf}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <Download size={14} />
                      <span>{loading ? 'Converting Workbook...' : 'CONVERT TO PDF'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 9: IMAGES TO PDF */}
            {activeTool === 'image-to-pdf' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Convert Images to PDF</h3>

                {/* Configurations */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 rounded-2xl bg-slate-900/40 border border-white/5">
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Page Size</label>
                    <select
                      value={imgPageSize}
                      onChange={(e) => setImgPageSize(e.target.value)}
                      className="w-full bg-slate-950 border border-white/10 rounded-lg p-2 text-xs text-white"
                    >
                      <option value="A4">A4</option>
                      <option value="LETTER">Letter</option>
                      <option value="ORIGINAL">Original Size</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Orientation</label>
                    <select
                      value={imgOrientation}
                      onChange={(e) => setImgOrientation(e.target.value)}
                      className="w-full bg-slate-950 border border-white/10 rounded-lg p-2 text-xs text-white"
                    >
                      <option value="AUTO">Auto Detect</option>
                      <option value="PORTRAIT">Portrait</option>
                      <option value="LANDSCAPE">Landscape</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Fit Mode</label>
                    <select
                      value={imgFitMode}
                      onChange={(e) => setImgFitMode(e.target.value)}
                      className="w-full bg-slate-950 border border-white/10 rounded-lg p-2 text-xs text-white"
                    >
                      <option value="FIT">Fit Aspect Ratio</option>
                      <option value="FILL">Fill Page</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Margins</label>
                    <select
                      value={imgMargins}
                      onChange={(e) => setImgMargins(e.target.value)}
                      className="w-full bg-slate-950 border border-white/10 rounded-lg p-2 text-xs text-white"
                    >
                      <option value="NONE">None (0pt)</option>
                      <option value="SMALL">Small (18pt)</option>
                      <option value="MEDIUM">Medium (36pt)</option>
                      <option value="LARGE">Large (54pt)</option>
                    </select>
                  </div>
                </div>

                {/* Upload Section */}
                <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                  <input
                    type="file"
                    multiple
                    accept=".png,.jpg,.jpeg"
                    onChange={handleMultipleFilesChange}
                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                  />
                  <div className="flex flex-col items-center space-y-2">
                    <Plus className="w-8 h-8 text-gray-400" />
                    <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add images</span>
                    <span className="text-[10px] text-gray-500">Supports PNG, JPG, JPEG (Max 20MB per image)</span>
                  </div>
                </div>

                {/* Uploaded Files List with Reordering */}
                {selectedFiles.length > 0 && (
                  <div className="space-y-3">
                    <div className="text-xs font-bold text-indigo-400">Uploaded Images ({selectedFiles.length})</div>
                    <div className="grid gap-2">
                      {selectedFiles.map((file, idx) => (
                        <div key={idx} className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                          <div className="flex items-center space-x-3 truncate">
                            <span className="text-[10px] font-bold text-gray-500">#{idx + 1}</span>
                            <FileText size={16} className="text-indigo-400 flex-shrink-0" />
                            <span className="text-xs text-gray-200 truncate max-w-[200px]">{file.name}</span>
                            <span className="text-[10px] text-gray-500 flex-shrink-0">({(file.size / (1024 * 1024)).toFixed(2)} MB)</span>
                          </div>
                          <div className="flex items-center space-x-2">
                            <button
                              disabled={idx === 0}
                              onClick={() => {
                                const arr = [...selectedFiles];
                                const tmp = arr[idx];
                                arr[idx] = arr[idx - 1];
                                arr[idx - 1] = tmp;
                                setSelectedFiles(arr);
                              }}
                              className="p-1 text-gray-400 hover:text-white disabled:opacity-30 transition-opacity"
                            >
                              ▲
                            </button>
                            <button
                              disabled={idx === selectedFiles.length - 1}
                              onClick={() => {
                                const arr = [...selectedFiles];
                                const tmp = arr[idx];
                                arr[idx] = arr[idx + 1];
                                arr[idx + 1] = tmp;
                                setSelectedFiles(arr);
                              }}
                              className="p-1 text-gray-400 hover:text-white disabled:opacity-30 transition-opacity"
                            >
                              ▼
                            </button>
                            <button onClick={() => removeSelectedFile(idx)} className="text-gray-400 hover:text-red-400 transition-colors ml-2">
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="pt-4 border-t border-white/5">
                      <button
                        onClick={handleImagesToPdf}
                        disabled={loading}
                        className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                      >
                        <Download size={14} />
                        <span>{loading ? 'Converting Images...' : 'CONVERT TO PDF'}</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 10: PDF TO WORD */}
            {activeTool === 'pdf-to-word' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Convert PDF to Editable Word (DOCX)</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add PDF file</span>
                      <span className="text-[10px] text-gray-500">Only PDF format accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => setSingleFile(null)} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {/* Limitation Note */}
                <div className="p-4 rounded-xl bg-indigo-950/30 border border-indigo-500/20 text-xs text-gray-300 space-y-1">
                  <div className="font-bold text-indigo-400">Important Conversion Notice:</div>
                  <p>Complex multi-column layouts, scanned/image-only PDFs, tables, custom embedded fonts, and advanced PDF drawing elements may not convert perfectly. OCR is not currently supported.</p>
                </div>

                {singleFile && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <button
                      onClick={handlePdfToWord}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <Download size={14} />
                      <span>{loading ? 'Converting PDF...' : 'CONVERT TO WORD'}</span>
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* TOOL 11: RESUME PARSER */}
            {activeTool === 'resume-parser' && (
              <div className="space-y-6">
                <h3 className="text-base font-bold text-white mb-2">Resume Intelligence / Parser</h3>

                {/* Upload Section */}
                {!singleFile ? (
                  <div className="border-2 border-dashed border-white/15 rounded-2xl p-8 text-center bg-slate-900/30 hover:border-indigo-500/50 transition-all relative">
                    <input
                      type="file"
                      accept=".pdf,.docx"
                      onChange={handleSingleFileChange}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <div className="flex flex-col items-center space-y-2">
                      <Plus className="w-8 h-8 text-gray-400" />
                      <span className="text-xs font-semibold text-gray-300">Drag & Drop or click to add Resume file</span>
                      <span className="text-[10px] text-gray-500">PDF and DOCX formats accepted (Max 20MB)</span>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-3 bg-slate-900/60 rounded-xl border border-white/5">
                    <div className="flex items-center space-x-3">
                      <FileText size={16} className="text-indigo-400" />
                      <div className="text-xs text-gray-200 truncate max-w-xs">{singleFile.name}</div>
                      <span className="text-[10px] text-gray-500">({(singleFile.size / (1024 * 1024)).toFixed(2)} MB)</span>
                    </div>
                    <button onClick={() => { setSingleFile(null); setParsedResume(null); }} className="text-gray-400 hover:text-red-400 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                )}

                {/* Limitation Note */}
                <div className="p-4 rounded-xl bg-indigo-950/30 border border-indigo-500/20 text-xs text-gray-300 space-y-1">
                  <div className="font-bold text-indigo-400">Important Note:</div>
                  <p>Complex multi-column layouts, scanned/image-only PDFs, tables, custom embedded fonts, and advanced PDF drawing elements may not parse perfectly. OCR is not currently supported.</p>
                </div>

                {singleFile && !parsedResume && (
                  <div className="space-y-4 pt-4 border-t border-white/5">
                    <button
                      onClick={handleResumeParse}
                      disabled={loading}
                      className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-xs shadow-lg flex items-center justify-center space-x-2"
                    >
                      <UserCheck size={14} />
                      <span>{loading ? 'Parsing Resume...' : 'PARSE RESUME'}</span>
                    </button>
                  </div>
                )}

                {/* Parsed Resume View cards */}
                {parsedResume && (
                  <div className="space-y-6 pt-6 border-t border-white/10">
                    <div className="flex justify-between items-center bg-slate-900/50 p-4 rounded-xl border border-white/5">
                      <div>
                        <h4 className="text-sm font-bold text-white">{parsedResume.profile.fullName || 'Unknown Name'}</h4>
                        <p className="text-xs text-gray-400 mt-1">{parsedResume.profile.summary || 'No summary provided.'}</p>
                      </div>
                      <div className="flex flex-col items-end">
                        <span className="text-[10px] text-gray-500 uppercase tracking-widest font-semibold">Confidence</span>
                        <div className="flex space-x-2 mt-1">
                          <span className={`px-2 py-0.5 rounded text-[9px] font-bold ${parsedResume.confidence?.profile === 'HIGH' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'}`}>
                            Profile: {parsedResume.confidence?.profile || 'MEDIUM'}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Contacts Card */}
                    <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-2">
                      <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Contact Information</h5>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                        <div><span className="text-gray-400">Email:</span> <span className="text-gray-200">{parsedResume.profile.contact?.email || 'N/A'}</span></div>
                        <div><span className="text-gray-400">Phone:</span> <span className="text-gray-200">{parsedResume.profile.contact?.phone || 'N/A'}</span></div>
                        {parsedResume.profile.contact?.linkedin && (
                          <div><span className="text-gray-400">LinkedIn:</span> <a href={parsedResume.profile.contact.linkedin} target="_blank" rel="noopener noreferrer" className="text-indigo-400 hover:underline">{parsedResume.profile.contact.linkedin}</a></div>
                        )}
                        {parsedResume.profile.contact?.github && (
                          <div><span className="text-gray-400">GitHub:</span> <a href={parsedResume.profile.contact.github} target="_blank" rel="noopener noreferrer" className="text-indigo-400 hover:underline">{parsedResume.profile.contact.github}</a></div>
                        )}
                        {parsedResume.profile.contact?.portfolio && (
                          <div><span className="text-gray-400">Portfolio:</span> <a href={parsedResume.profile.contact.portfolio} target="_blank" rel="noopener noreferrer" className="text-indigo-400 hover:underline">{parsedResume.profile.contact.portfolio}</a></div>
                        )}
                      </div>
                    </div>

                    {/* Skills Card */}
                    <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-3">
                      <div className="flex justify-between items-center">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Skills Categorized</h5>
                        <span className={`px-2 py-0.5 rounded text-[9px] font-bold ${parsedResume.confidence?.skills === 'HIGH' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                          Skills: {parsedResume.confidence?.skills || 'LOW'}
                        </span>
                      </div>
                      {Object.keys(parsedResume.skills || {}).length > 0 ? (
                        <div className="space-y-2">
                          {Object.entries(parsedResume.skills).map(([category, items]) => (
                            <div key={category} className="text-xs">
                              <span className="text-gray-400 font-semibold">{category}: </span>
                              <div className="flex flex-wrap gap-1 mt-1">
                                {items.map((it) => (
                                  <span key={it} className="px-2 py-0.5 bg-indigo-900/40 text-indigo-300 rounded text-[10px]">
                                    {it}
                                  </span>
                                ))}
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-gray-500">No matching skills found in the skill dictionary.</p>
                      )}
                    </div>

                    {/* Education Card */}
                    <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-3">
                      <div className="flex justify-between items-center">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Education</h5>
                        <span className={`px-2 py-0.5 rounded text-[9px] font-bold ${parsedResume.confidence?.education === 'HIGH' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                          Education: {parsedResume.confidence?.education || 'LOW'}
                        </span>
                      </div>
                      {parsedResume.education && parsedResume.education.length > 0 ? (
                        <div className="space-y-2">
                          {parsedResume.education.map((edu, idx) => (
                            <div key={idx} className="text-xs border-b border-white/5 pb-2 last:border-0 last:pb-0">
                              <div className="font-bold text-gray-200">{edu.degree} - {edu.fieldOfStudy}</div>
                              <div className="text-gray-400 mt-0.5">{edu.institution}</div>
                              <div className="text-[10px] text-gray-500 mt-0.5">Year: {edu.startYear || 'N/A'} - {edu.endYear || 'N/A'} | CGPA: {edu.cgpa || 'N/A'}</div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-gray-500">No education entries detected.</p>
                      )}
                    </div>

                    {/* Experience Card */}
                    <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-3">
                      <div className="flex justify-between items-center">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Experience</h5>
                        <span className={`px-2 py-0.5 rounded text-[9px] font-bold ${parsedResume.confidence?.experience === 'HIGH' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                          Experience: {parsedResume.confidence?.experience || 'LOW'}
                        </span>
                      </div>
                      {parsedResume.experience && parsedResume.experience.length > 0 ? (
                        <div className="space-y-3">
                          {parsedResume.experience.map((exp, idx) => (
                            <div key={idx} className="text-xs border-b border-white/5 pb-2 last:border-0 last:pb-0">
                              <div className="font-bold text-gray-200">{exp.title}</div>
                              <div className="text-gray-400 mt-0.5">{exp.company}</div>
                              {exp.description && exp.description.length > 0 && (
                                <ul className="list-disc list-inside mt-1 text-gray-400 text-[11px] space-y-0.5">
                                  {exp.description.map((desc, dIdx) => (
                                    <li key={dIdx}>{desc}</li>
                                  ))}
                                </ul>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-gray-500">No experience entries detected.</p>
                      )}
                    </div>

                    {/* Projects Card */}
                    {parsedResume.projects && parsedResume.projects.length > 0 && (
                      <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-3">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Projects</h5>
                        <div className="space-y-3">
                          {parsedResume.projects.map((proj, idx) => (
                            <div key={idx} className="text-xs border-b border-white/5 pb-2 last:border-0 last:pb-0">
                              <div className="font-bold text-gray-200">{proj.projectName}</div>
                              <p className="text-gray-400 mt-1 text-[11px]">{proj.description}</p>
                              {proj.technologies && proj.technologies.length > 0 && (
                                <div className="flex flex-wrap gap-1 mt-1">
                                  {proj.technologies.map((t) => (
                                    <span key={t} className="px-1.5 py-0.5 bg-purple-900/30 text-purple-300 rounded text-[9px]">
                                      {t}
                                    </span>
                                  ))}
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Certifications Card */}
                    {parsedResume.certifications && parsedResume.certifications.length > 0 && (
                      <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-2">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Certifications</h5>
                        <div className="space-y-1 text-xs">
                          {parsedResume.certifications.map((cert, idx) => (
                            <div key={idx} className="text-gray-200">
                              • {cert.name} <span className="text-gray-500">({cert.issuer})</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Achievements Card */}
                    {parsedResume.achievements && parsedResume.achievements.length > 0 && (
                      <div className="bg-slate-900/50 p-4 rounded-xl border border-white/5 space-y-2">
                        <h5 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Achievements</h5>
                        <div className="space-y-1 text-xs text-gray-300">
                          {parsedResume.achievements.map((ach, idx) => (
                            <div key={idx}>• {ach}</div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}

          </div>
        </main>
      </div>

      <SOSModal isOpen={isSOSOpen} onClose={() => setIsSOSOpen(false)} />
    </div>
  );
}
