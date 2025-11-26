import React from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Link from '@mui/material/Link';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Collapse from '@mui/material/Collapse';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import { Typography } from '@mui/material';


function TabPanel(props) {
  const { children, value, index, ...other} = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`help-tabpanel-${index}`}
      aria-labelledby={`help-tab-${index}`}
      style={{ 
        display: value === index ? 'flex' : 'none',
        flexGrow: 1,
        height: '100%',
        width: '100%',
      }}
      {...other}
    >
      {value === index && children}
    </div>
  );
}

// Table of contents structure for the documentation
const tocStructure = {
  en: [
    { title: 'Introduction', url: 'index.html' },
    {
      title: 'Source Files Format',
      url: 'source-files-format.html',
      children: [
        { title: 'Memory Limits', url: 'source-files-format.html#memory-limits' },
        { title: 'The .data Section', url: 'source-files-format.html#the-data-section' },
        { title: 'The .code Section', url: 'source-files-format.html#the-code-section' },
        { title: 'The #include Command', url: 'source-files-format.html#the-include-command' },
      ],
    },
    {
      title: 'Instruction Set',
      url: 'instructions.html',
      children: [
        { title: 'ALU Instructions', url: 'instructions.html#alu-instructions' },
        { title: 'Load/Store Instructions', url: 'instructions.html#load-store-instructions' },
        { title: 'Flow Control Instructions', url: 'instructions.html#flow-control-instructions' },
        { title: 'SYSCALL Instruction', url: 'instructions.html#the-syscall-instruction' },
        { title: 'Other Instructions', url: 'instructions.html#other-instructions' },
      ],
    },
    {
      title: 'Floating Point Unit',
      url: 'fpu.html',
      children: [
        { title: 'Special Values', url: 'fpu.html#special-values' },
        { title: 'Exception Configuration', url: 'fpu.html#exception-configuration' },
        { title: 'The .double Directive', url: 'fpu.html#the-double-directive' },
        { title: 'The FCSR Register', url: 'fpu.html#the-fcsr-register' },
        { title: 'Instruction Set', url: 'fpu.html#instruction-set' },
      ],
    },
    {
      title: 'User Interface',
      url: 'user-interface.html',
      children: [
        { title: 'The Menu Bar', url: 'user-interface.html#the-menu-bar' },
        { title: 'Frames', url: 'user-interface.html#frames' },
        { title: 'Dialogs', url: 'user-interface.html#dialogs' },
        { title: 'Command Line Options', url: 'user-interface.html#command-line-options' },
      ],
    },
    {
      title: 'Code Examples',
      url: 'examples.html',
      children: [
        { title: 'SYSCALL', url: 'examples.html#syscall' },
      ],
    },
  ],
  it: [
    { title: 'Introduzione', url: 'index.html' },
    {
      title: 'Formato dei File Sorgente',
      url: 'source-files-format.html',
      children: [
        { title: 'Limiti di Memoria', url: 'source-files-format.html#memory-limits' },
        { title: 'La Sezione .data', url: 'source-files-format.html#the-data-section' },
        { title: 'La Sezione .code', url: 'source-files-format.html#the-code-section' },
        { title: 'Il Comando #include', url: 'source-files-format.html#the-include-command' },
      ],
    },
    {
      title: 'Set di Istruzioni',
      url: 'instructions.html',
      children: [
        { title: 'Istruzioni ALU', url: 'instructions.html#alu-instructions' },
        { title: 'Istruzioni Load/Store', url: 'instructions.html#load-store-instructions' },
        { title: 'Istruzioni di Controllo del Flusso', url: 'instructions.html#flow-control-instructions' },
        { title: 'Istruzione SYSCALL', url: 'instructions.html#the-syscall-instruction' },
        { title: 'Altre Istruzioni', url: 'instructions.html#other-instructions' },
      ],
    },
    {
      title: 'Unità a Virgola Mobile',
      url: 'fpu.html',
      children: [
        { title: 'Valori Speciali', url: 'fpu.html#special-values' },
        { title: 'Configurazione Eccezioni', url: 'fpu.html#exception-configuration' },
        { title: 'La Direttiva .double', url: 'fpu.html#the-double-directive' },
        { title: 'Il Registro FCSR', url: 'fpu.html#the-fcsr-register' },
        { title: 'Set di Istruzioni', url: 'fpu.html#instruction-set' },
      ],
    },
    {
      title: 'Interfaccia Utente',
      url: 'user-interface.html',
      children: [
        { title: 'La Barra dei Menu', url: 'user-interface.html#the-menu-bar' },
        { title: 'Frame', url: 'user-interface.html#frames' },
        { title: 'Dialoghi', url: 'user-interface.html#dialogs' },
        { title: 'Opzioni da Riga di Comando', url: 'user-interface.html#command-line-options' },
      ],
    },
    {
      title: 'Esempi di Codice',
      url: 'examples.html',
      children: [
        { title: 'SYSCALL', url: 'examples.html#syscall' },
      ],
    },
  ],
  zh: [
    { title: '简介', url: 'index.html' },
    {
      title: '源文件格式',
      url: 'source-files-format.html',
      children: [
        { title: '内存限制', url: 'source-files-format.html#memory-limits' },
        { title: '.data 部分', url: 'source-files-format.html#the-data-section' },
        { title: '.code 部分', url: 'source-files-format.html#the-code-section' },
        { title: '#include 命令', url: 'source-files-format.html#the-include-command' },
      ],
    },
    {
      title: '指令集',
      url: 'instructions.html',
      children: [
        { title: 'ALU 指令', url: 'instructions.html#alu-instructions' },
        { title: '加载/存储指令', url: 'instructions.html#load-store-instructions' },
        { title: '流程控制指令', url: 'instructions.html#flow-control-instructions' },
        { title: 'SYSCALL 指令', url: 'instructions.html#the-syscall-instruction' },
        { title: '其他指令', url: 'instructions.html#other-instructions' },
      ],
    },
    {
      title: '浮点单元',
      url: 'fpu.html',
      children: [
        { title: '特殊值', url: 'fpu.html#special-values' },
        { title: '异常配置', url: 'fpu.html#exception-configuration' },
        { title: '.double 指令', url: 'fpu.html#the-double-directive' },
        { title: 'FCSR 寄存器', url: 'fpu.html#the-fcsr-register' },
        { title: '指令集', url: 'fpu.html#instruction-set' },
      ],
    },
    {
      title: '用户界面',
      url: 'user-interface.html',
      children: [
        { title: '菜单栏', url: 'user-interface.html#the-menu-bar' },
        { title: '框架', url: 'user-interface.html#frames' },
        { title: '对话框', url: 'user-interface.html#dialogs' },
        { title: '命令行选项', url: 'user-interface.html#command-line-options' },
      ],
    },
    {
      title: '代码示例',
      url: 'examples.html',
      children: [
        { title: 'SYSCALL', url: 'examples.html#syscall' },
      ],
    },
  ],
};

function NavigationDrawer({ language, onNavigate, currentPage }) {
  const [openItems, setOpenItems] = React.useState({});

  const handleToggle = (index) => {
    setOpenItems((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  };

  const toc = tocStructure[language] || tocStructure.en;

  return (
    <Box sx={{ width: 280, height: '100%', overflowY: 'auto' }}>
      <List dense>
        {toc.map((item, index) => (
          <React.Fragment key={index}>
            <ListItem disablePadding>
              <ListItemButton
                onClick={() => {
                  if (item.children) {
                    handleToggle(index);
                  } else {
                    onNavigate(item.url);
                  }
                }}
                selected={currentPage === item.url}
              >
                <ListItemText 
                  primary={item.title}
                  primaryTypographyProps={{
                    fontSize: '0.9rem',
                    fontWeight: currentPage === item.url ? 600 : 400,
                  }}
                />
                {item.children && (openItems[index] ? <ExpandLess /> : <ExpandMore />)}
              </ListItemButton>
            </ListItem>
            {item.children && (
              <Collapse in={openItems[index]} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  {item.children.map((child, childIndex) => (
                    <ListItem key={childIndex} disablePadding>
                      <ListItemButton
                        sx={{ pl: 4 }}
                        onClick={() => onNavigate(child.url)}
                        selected={currentPage === child.url}
                      >
                        <ListItemText
                          primary={child.title}
                          primaryTypographyProps={{
                            fontSize: '0.85rem',
                            fontWeight: currentPage === child.url ? 600 : 400,
                          }}
                        />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
              </Collapse>
            )}
          </React.Fragment>
        ))}
      </List>
    </Box>
  );
}

export default function HelpDialog(props) {
  const [tabValue, setTabValue] = React.useState(0);
  const [language, setLanguage] = React.useState('en');
  const [currentPage, setCurrentPage] = React.useState('index.html');
  const iframeRef = React.useRef(null);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleLanguageChange = (event) => {
    setLanguage(event.target.value);
    setCurrentPage('index.html');
  };

  const handleNavigate = (url) => {
    setCurrentPage(url);
    if (iframeRef.current) {
      iframeRef.current.src = `docs/${language}/html/${url}`;
    }
  };

  // Inject custom CSS into iframe after it loads
  const handleIframeLoad = () => {
    try {
      const iframe = iframeRef.current;
      if (iframe && iframe.contentDocument) {
        const doc = iframe.contentDocument;
        
        // Check if custom CSS is already injected
        if (!doc.getElementById('custom-help-styles')) {
          const style = doc.createElement('style');
          style.id = 'custom-help-styles';
          style.textContent = `
            body {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
                'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
                sans-serif !important;
              -webkit-font-smoothing: antialiased;
              -moz-osx-font-smoothing: grayscale;
              line-height: 1.6 !important;
              color: #333 !important;
              padding: 24px !important;
              background: #fff !important;
            }
            h1, h2, h3, h4, h5, h6 {
              font-family: inherit !important;
              font-weight: 600 !important;
              margin-top: 24px !important;
              margin-bottom: 16px !important;
              color: #1a1a1a !important;
            }
            h1 {
              font-size: 2rem !important;
              border-bottom: 2px solid #e0e0e0 !important;
              padding-bottom: 8px !important;
            }
            h2 { font-size: 1.5rem !important; }
            h3 { font-size: 1.25rem !important; }
            a {
              color: #1976d2 !important;
              text-decoration: none !important;
            }
            a:hover { text-decoration: underline !important; }
            pre {
              background: #f5f5f5 !important;
              border: 1px solid #e0e0e0 !important;
              border-radius: 4px !important;
              padding: 16px !important;
              overflow-x: auto !important;
            }
            code {
              background: #f5f5f5 !important;
              border-radius: 3px !important;
              padding: 2px 6px !important;
              font-size: 0.875rem !important;
            }
            pre code {
              background: transparent !important;
              padding: 0 !important;
            }
            table {
              border-collapse: collapse !important;
              margin: 16px 0 !important;
            }
            th, td {
              border: 1px solid #e0e0e0 !important;
              padding: 12px !important;
            }
            th {
              background: #f5f5f5 !important;
              font-weight: 600 !important;
            }
            .related, .sphinxsidebar, div.clearer {
              display: none !important;
            }
            .documentwrapper, .bodywrapper, .body {
              margin: 0 !important;
              width: 100% !important;
            }
          `;
          doc.head.appendChild(style);
        }
      }
    } catch (e) {
      // Ignore cross-origin errors
      console.warn('Could not inject styles into iframe:', e);
    }
  };

  return (
    <Dialog 
      onClose={props.handleClose} 
      open={props.open}
      maxWidth="xl"
      fullWidth
      PaperProps={{
        sx: {
          height: '90vh',
        },
      }}
    >
      <DialogTitle className='help-title'>
        <Typography variant="h4">
          EduMIPS64 Web Frontend
        </Typography>
      </DialogTitle>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="help tabs">
          <Tab label="User Manual" id="help-tab-0" />
          <Tab label="About" id="help-tab-1" />
        </Tabs>
      </Box>
      <DialogContent className='help-content' sx={{ p: 0, display: 'flex', flexGrow: 1, overflow: 'hidden' }}>
        <TabPanel value={tabValue} index={0}>
          <Box sx={{ display: 'flex', height: '100%', width: '100%' }}>
            {/* Navigation Drawer */}
            <Box
              sx={{
                width: 280,
                flexShrink: 0,
                borderRight: 1,
                borderColor: 'divider',
                bgcolor: 'background.paper',
              }}
            >
              <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                <FormControl size="small" fullWidth>
                  <InputLabel id="language-select-label">Language</InputLabel>
                  <Select
                    labelId="language-select-label"
                    id="language-select"
                    value={language}
                    label="Language"
                    onChange={handleLanguageChange}
                  >
                    <MenuItem value="en">English</MenuItem>
                    <MenuItem value="it">Italiano</MenuItem>
                    <MenuItem value="zh">中文</MenuItem>
                  </Select>
                </FormControl>
              </Box>
              <NavigationDrawer
                language={language}
                onNavigate={handleNavigate}
                currentPage={currentPage}
              />
            </Box>

            {/* Content Area */}
            <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
              <iframe
                ref={iframeRef}
                src={`docs/${language}/html/${currentPage}`}
                onLoad={handleIframeLoad}
                style={{
                  width: '100%',
                  height: '100%',
                  border: 'none',
                }}
                title="EduMIPS64 User Manual"
                id="help-iframe"
              />
            </Box>
          </Box>
        </TabPanel>
        <TabPanel value={tabValue} index={1}>
          <Box sx={{ p: 3 }}>
            <Typography>Version: {props.ver}</Typography>
            <Typography gutterBottom variant="h6" sx={{ mt: 2 }}>
              Quick Start
            </Typography>
            <Typography gutterBottom>
              Once you load a program, hover over any instruction to see information
              about it. You will be able to see the address, its binary
              representation, the opcode and the CPU stage in which the instruction
              is in the current step, if it is in the pipeline.
            </Typography>
            <Typography gutterBottom>
              CPU stages are also encoded by colors.
            </Typography>
            <Typography gutterBottom variant="h6" sx={{ mt: 2 }}>
              About EduMIPS64
            </Typography>
            <Typography gutterBottom>
              This is the web version of the{' '}
              <Link href="https://www.edumips.org">EduMIPS64 CPU simulator</Link>.
            </Typography>
            <Typography gutterBottom>
              This is currently <strong>work-in-progress</strong>, very early stages
              and not fully functional. See{' '}
              <Link
                href="https://github.com/EduMIPS64/edumips64/issues?q=is%3Aissue+is%3Aopen+label%3Acomponent%3Aweb-ui"
                target="_blank"
                rel="noreferrer"
              >
                known issues
              </Link>
              .
            </Typography>
            <Typography gutterBottom>
              The core of the simulator is cross-compiled from Java to JavaScript,
              and the UI is developed with React.
            </Typography>
            <Typography gutterBottom>
              If you are interested in the evolution of this web application or want
              to contribute to it, please get in touch via{' '}
              <Link
                href="https://github.com/EduMIPS64/edumips64"
                target="_blank"
                rel="noreferrer"
              >
                GitHub
              </Link>
              !
            </Typography>
          </Box>
        </TabPanel>
      </DialogContent>
      <DialogActions>
        <Button onClick={props.handleClose} variant="outlined">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}
