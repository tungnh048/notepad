/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tungnh.views;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;

/**
 *
 * @author tung
 */
public class NotepadJFrame extends javax.swing.JFrame {

    String title = "Untitled - My Text Editor", pathFile = "", curDir = System.getProperty("user.home");
    boolean isSaved = true;
    UndoManager um = new UndoManager();
    Clipboard clb = Toolkit.getDefaultToolkit().getSystemClipboard();

    private void newFile() {
        title = "Untitled - My Text Editor";
        setTitle(title);
        txtContent.setText("");
        pathFile = "";
        isSaved = true;
        um.discardAllEdits();
        disableMenuItem();
    }
    
    private boolean saveAs(String content) {
        try {
            JFileChooser jf = new JFileChooser(curDir);
            jf.setFileFilter(new FileNameExtensionFilter("Text Document", "TXT"));
            int opt = jf.showSaveDialog(this);
            curDir = jf.getCurrentDirectory().getAbsolutePath();
            if (opt == JFileChooser.APPROVE_OPTION) {
                File f = jf.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".txt")) {
                    f = new File(f.getPath() + ".txt");
                }
                pathFile = f.getPath();
                if (f.exists()) {
                    int choice = JOptionPane.showConfirmDialog(this, f.getName() + " already exists.\nDo you want to replace it?", "Confirm Save As", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        Files.write(f.toPath(), content.getBytes());
                        isSaved = true;
                        title = f.getName().replaceAll("\\.txt$", "") + " - My Text Editor";
                        setTitle(title);
                    } else {
                        if (!saveAs(content))
                            return false;
                    }
                } else {
                    Files.write(f.toPath(), content.getBytes());
                    isSaved = true;
                    title = f.getName().replaceAll("\\.txt$", "") + " - My Text Editor";
                    setTitle(title);
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(NotepadJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    private boolean saveFile(String content) {
        if (pathFile.isEmpty()) {
            if (saveAs(content))
                return true;
        } else {
            try {
                Files.write(new File(pathFile).toPath(), content.getBytes());
                isSaved = true;
                return true;
            } catch (IOException ex) {
                Logger.getLogger(NotepadJFrame.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }
    
    private void openFile() {
        try {
            JFileChooser jf = new JFileChooser(curDir);
            jf.setFileFilter(new FileNameExtensionFilter("Text Document", "TXT"));
            int opt = jf.showOpenDialog(this);
            curDir = jf.getCurrentDirectory().getAbsolutePath();
            if (opt == JFileChooser.APPROVE_OPTION) {
                File f = jf.getSelectedFile();
                pathFile = f.getPath();
                title = f.getName().replaceAll("\\.txt$", "") + " - My Text Editor";                
                setTitle(title);
                um.discardAllEdits();
                try {
                    txtContent.read(Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8), null);
                } catch (MalformedInputException mie) {
                    txtContent.read(Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_16), null);
                }
                initListener();
                disableMenuItem();
            }
        } catch (IOException ex) {
            Logger.getLogger(NotepadJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    private void disableMenuItem() {
        if (um.canUndo()) {
            mUndo.setEnabled(true);
        } else {
            mUndo.setEnabled(false);
        }
        
        if (um.canRedo()) {
            mRedo.setEnabled(true);
        } else {
            mRedo.setEnabled(false);
        }
        
        if (txtContent.getText().isEmpty()) {
            mFind.setEnabled(false);
        } else {
            mFind.setEnabled(true);
        }
        
        if (um.canUndo())
            isSaved = false;
        else {
            isSaved = true;
        }
    }
    
    private void initListener() {
        txtContent.getDocument().addUndoableEditListener((UndoableEditEvent e) -> {
            um.addEdit(e.getEdit());
            disableMenuItem();
        });
        
        txtContent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                disableMenuItem();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                disableMenuItem();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                disableMenuItem();
            }
        });
    }
    
    private void initFindDialog() {
        findDialog.setAlwaysOnTop(true);
        findDialog.setTitle("Find");
        findDialog.setSize(450, 200);
        findDialog.setLocationRelativeTo(this);  
    }
    
    private void initReplaceDialog() {
        replaceDialog.setAlwaysOnTop(true);
        replaceDialog.setTitle("Replace");
        replaceDialog.setSize(500, 250);
        replaceDialog.setLocationRelativeTo(this);
    }
    
    private void findStr() {
        String find = txtFind.getText();
        int index;
        if (rbtnUp.isSelected()) {
            index = txtContent.getText().lastIndexOf(find, txtContent.getSelectionStart() - 1);
        } else {
            index = txtContent.getText().indexOf(find, txtContent.getCaretPosition());
        }
        if (index < 0) {
            findDialog.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(this, "Cannot find \"" + find + "\"", "My Text Editor", JOptionPane.INFORMATION_MESSAGE);
            findDialog.setAlwaysOnTop(true);
        } else {
            txtContent.select(index, index + find.length());
        }
    }
    
    private void repFindStr() {
        String find = txtReFind.getText();
        int index = txtContent.getText().indexOf(find, txtContent.getCaretPosition());
        if (index < 0) {
            replaceDialog.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(this, "Cannot find \"" + find + "\"", "My Text Editor", JOptionPane.INFORMATION_MESSAGE);
            replaceDialog.setAlwaysOnTop(true);
        } else {
            txtContent.select(index, index + find.length());            
        }
    }
    
    private void initExit() {
        if (isSaved) {
            System.exit(0);
        } else {
            int opt = JOptionPane.showConfirmDialog(this, "Do you want to save changes to " + title.split(" - ")[0] + "?", "My Text Editor", JOptionPane.YES_NO_CANCEL_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                if (saveFile(txtContent.getText())) {
                    System.exit(0);
                }
            } else if (opt == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }
    
    
    /**
     * Creates new form NotepadJFrame
     */  
    public NotepadJFrame() {
        initComponents();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                initExit();
            }
        });
        this.setLocationRelativeTo(null);
        setTitle(title);
        um.setLimit(-1);
        initListener();
        mCut.setEnabled(false);
        mCopy.setEnabled(false);
        if (clb.getContents(null) == null) {
            mPaste.setEnabled(false);
        } else {
            System.out.println(clb.getContents(null).toString());
            mPaste.setEnabled(true);
        }
        mUndo.setEnabled(false);
        mRedo.setEnabled(false);
        mFind.setEnabled(false);
        initFindDialog();
        initReplaceDialog();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        findDialog = new javax.swing.JDialog();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        btnFind = new javax.swing.JButton();
        btnCancelFind = new javax.swing.JButton();
        pnlFindDirection = new javax.swing.JPanel();
        rbtnUp = new javax.swing.JRadioButton();
        rbtnDown = new javax.swing.JRadioButton();
        replaceDialog = new javax.swing.JDialog();
        lblReFind = new javax.swing.JLabel();
        txtReFind = new javax.swing.JTextField();
        lblReplace = new javax.swing.JLabel();
        txtReplace = new javax.swing.JTextField();
        btnFindNext = new javax.swing.JButton();
        btnReplace = new javax.swing.JButton();
        btnReplaceAll = new javax.swing.JButton();
        btnCancelReplace = new javax.swing.JButton();
        btnGroupDirection = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtContent = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        mNew = new javax.swing.JMenuItem();
        mOpen = new javax.swing.JMenuItem();
        mSave = new javax.swing.JMenuItem();
        mSaveAs = new javax.swing.JMenuItem();
        mExit = new javax.swing.JMenuItem();
        menuEdit = new javax.swing.JMenu();
        mSelectAll = new javax.swing.JMenuItem();
        mCut = new javax.swing.JMenuItem();
        mCopy = new javax.swing.JMenuItem();
        mPaste = new javax.swing.JMenuItem();
        mUndo = new javax.swing.JMenuItem();
        mRedo = new javax.swing.JMenuItem();
        mFind = new javax.swing.JMenuItem();
        mReplace = new javax.swing.JMenuItem();
        mChangeFont = new javax.swing.JMenuItem();

        lblFind.setText("Find What:");

        txtFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFindKeyReleased(evt);
            }
        });

        btnFind.setText("Find Next");
        btnFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindActionPerformed(evt);
            }
        });

        btnCancelFind.setText("Cancel");

        pnlFindDirection.setBorder(javax.swing.BorderFactory.createTitledBorder("Direction"));

        btnGroupDirection.add(rbtnUp);
        rbtnUp.setText("Up");

        btnGroupDirection.add(rbtnDown);
        rbtnDown.setSelected(true);
        rbtnDown.setText("Down");

        javax.swing.GroupLayout pnlFindDirectionLayout = new javax.swing.GroupLayout(pnlFindDirection);
        pnlFindDirection.setLayout(pnlFindDirectionLayout);
        pnlFindDirectionLayout.setHorizontalGroup(
            pnlFindDirectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFindDirectionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbtnUp)
                .addGap(18, 18, 18)
                .addComponent(rbtnDown)
                .addContainerGap())
        );
        pnlFindDirectionLayout.setVerticalGroup(
            pnlFindDirectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFindDirectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFindDirectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtnUp)
                    .addComponent(rbtnDown))
                .addContainerGap())
        );

        javax.swing.GroupLayout findDialogLayout = new javax.swing.GroupLayout(findDialog.getContentPane());
        findDialog.getContentPane().setLayout(findDialogLayout);
        findDialogLayout.setHorizontalGroup(
            findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, findDialogLayout.createSequentialGroup()
                        .addComponent(lblFind)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFind, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, findDialogLayout.createSequentialGroup()
                        .addComponent(pnlFindDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)))
                .addGroup(findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCancelFind, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFind, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        findDialogLayout.setVerticalGroup(
            findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findDialogLayout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFind)
                    .addComponent(txtFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFind))
                .addGroup(findDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(findDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelFind))
                    .addGroup(findDialogLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(pnlFindDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        lblReFind.setText("Find What:");

        txtReFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtReFindKeyReleased(evt);
            }
        });

        lblReplace.setText("Replace With:");

        btnFindNext.setText("Find Next");
        btnFindNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindNextActionPerformed(evt);
            }
        });

        btnReplace.setText("Replace");
        btnReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReplaceActionPerformed(evt);
            }
        });

        btnReplaceAll.setText("Replace All");
        btnReplaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReplaceAllActionPerformed(evt);
            }
        });

        btnCancelReplace.setText("Cancel");

        javax.swing.GroupLayout replaceDialogLayout = new javax.swing.GroupLayout(replaceDialog.getContentPane());
        replaceDialog.getContentPane().setLayout(replaceDialogLayout);
        replaceDialogLayout.setHorizontalGroup(
            replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replaceDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblReFind, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblReplace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(replaceDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, replaceDialogLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(txtReFind, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnReplace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnReplaceAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancelReplace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFindNext, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        replaceDialogLayout.setVerticalGroup(
            replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replaceDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReFind)
                    .addComponent(txtReFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFindNext))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(replaceDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReplace)
                    .addComponent(txtReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblReplace))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReplaceAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelReplace)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtContent.setColumns(20);
        txtContent.setLineWrap(true);
        txtContent.setRows(5);
        txtContent.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtContent);

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jMenuBar1.setPreferredSize(new java.awt.Dimension(66, 30));

        menuFile.setText("File");
        menuFile.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N

        mNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mNew.setText("New");
        mNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mNewActionPerformed(evt);
            }
        });
        menuFile.add(mNew);

        mOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mOpen.setText("Open...");
        mOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mOpenActionPerformed(evt);
            }
        });
        menuFile.add(mOpen);

        mSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mSave.setText("Save");
        mSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSaveActionPerformed(evt);
            }
        });
        menuFile.add(mSave);

        mSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mSaveAs.setText("Save As...");
        mSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(mSaveAs);

        mExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.ALT_DOWN_MASK));
        mExit.setText("Exit");
        mExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExitActionPerformed(evt);
            }
        });
        menuFile.add(mExit);

        jMenuBar1.add(menuFile);

        menuEdit.setText("Edit");
        menuEdit.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        menuEdit.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuEditMenuSelected(evt);
            }
        });

        mSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mSelectAll.setText("Select All");
        mSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSelectAllActionPerformed(evt);
            }
        });
        menuEdit.add(mSelectAll);

        mCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mCut.setText("Cut");
        mCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCutActionPerformed(evt);
            }
        });
        menuEdit.add(mCut);

        mCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mCopy.setText("Copy");
        mCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCopyActionPerformed(evt);
            }
        });
        menuEdit.add(mCopy);

        mPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mPaste.setText("Paste");
        mPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPasteActionPerformed(evt);
            }
        });
        menuEdit.add(mPaste);

        mUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mUndo.setText("Undo");
        mUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mUndoActionPerformed(evt);
            }
        });
        menuEdit.add(mUndo);

        mRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mRedo.setText("Redo");
        mRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRedoActionPerformed(evt);
            }
        });
        menuEdit.add(mRedo);

        mFind.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mFind.setText("Find");
        mFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mFindActionPerformed(evt);
            }
        });
        menuEdit.add(mFind);

        mReplace.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mReplace.setText("Replace");
        mReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mReplaceActionPerformed(evt);
            }
        });
        menuEdit.add(mReplace);

        mChangeFont.setText("Change font");
        menuEdit.add(mChangeFont);

        jMenuBar1.add(menuEdit);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mNewActionPerformed
        if (isSaved) {
            newFile();
        } else {
            int opt = JOptionPane.showConfirmDialog(this, "Do you want to save changes to " + title.split(" - ")[0] + "?", "My Text Editor", JOptionPane.YES_NO_CANCEL_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                if (saveFile(txtContent.getText())) {
                    newFile();
                }
            } else if (opt == JOptionPane.NO_OPTION) {
                newFile();
            }
        }
    }//GEN-LAST:event_mNewActionPerformed

    private void mOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mOpenActionPerformed
        if (isSaved) {
            openFile();
        } else {
            int opt = JOptionPane.showConfirmDialog(this, "Do you want to save changes to " + title.split(" - ")[0] + "?", "My Text Editor", JOptionPane.YES_NO_CANCEL_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                if (saveFile(txtContent.getText())) {
                    openFile();
                }
            } else if (opt == JOptionPane.NO_OPTION) {
                openFile();
            }
        }
    }//GEN-LAST:event_mOpenActionPerformed

    private void mSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSaveActionPerformed
        saveFile(txtContent.getText());
    }//GEN-LAST:event_mSaveActionPerformed

    private void mSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSaveAsActionPerformed
        saveAs(txtContent.getText());
    }//GEN-LAST:event_mSaveAsActionPerformed

    private void mExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mExitActionPerformed
        initExit();
    }//GEN-LAST:event_mExitActionPerformed

    private void mSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSelectAllActionPerformed
        txtContent.selectAll();
    }//GEN-LAST:event_mSelectAllActionPerformed

    private void mCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCutActionPerformed
        txtContent.cut();
    }//GEN-LAST:event_mCutActionPerformed

    private void mCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCopyActionPerformed
        txtContent.copy();
    }//GEN-LAST:event_mCopyActionPerformed

    private void mPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPasteActionPerformed
        txtContent.paste();
    }//GEN-LAST:event_mPasteActionPerformed

    private void menuEditMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuEditMenuSelected
        if (txtContent.getSelectedText() == null) {
            mCut.setEnabled(false);
            mCopy.setEnabled(false);
        } else {
            mCut.setEnabled(true);
            mCopy.setEnabled(true);
        }        
        
        if (clb.getContents(null) == null) {
            mPaste.setEnabled(false);
        } else {
            mPaste.setEnabled(true);
        }
    }//GEN-LAST:event_menuEditMenuSelected

    private void mUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mUndoActionPerformed
        um.undo();
    }//GEN-LAST:event_mUndoActionPerformed

    private void mRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRedoActionPerformed
        um.redo();
    }//GEN-LAST:event_mRedoActionPerformed

    private void mFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mFindActionPerformed
        findDialog.setVisible(true); 
        if (txtContent.getSelectedText() != null) {
            txtFind.setText(txtContent.getSelectedText());
        }
    }//GEN-LAST:event_mFindActionPerformed

    private void mReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mReplaceActionPerformed
        replaceDialog.setVisible(true);
        if (txtContent.getSelectedText() != null) {
            txtReFind.setText(txtContent.getSelectedText());
        }
    }//GEN-LAST:event_mReplaceActionPerformed

    private void btnFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindActionPerformed
        findStr();
    }//GEN-LAST:event_btnFindActionPerformed

    private void btnFindNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindNextActionPerformed
        repFindStr();
    }//GEN-LAST:event_btnFindNextActionPerformed

    private void btnReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReplaceActionPerformed
        if (txtContent.getSelectedText() == null) {
            repFindStr();
        } else {
            txtContent.replaceSelection(txtReplace.getText());
            repFindStr();
        }
    }//GEN-LAST:event_btnReplaceActionPerformed

    private void txtFindKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFindKeyReleased
        if (txtFind.getText().isEmpty()) {
            btnFind.setEnabled(false);
        } else {
            btnFind.setEnabled(true);
        }
    }//GEN-LAST:event_txtFindKeyReleased

    private void txtReFindKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtReFindKeyReleased
        if (txtReFind.getText().isEmpty()) {
            btnFindNext.setEnabled(false);
            btnReplace.setEnabled(false);
            btnReplaceAll.setEnabled(false);
        } else {
            btnFindNext.setEnabled(true);
            btnReplace.setEnabled(true);
            btnReplaceAll.setEnabled(true);
        }
    }//GEN-LAST:event_txtReFindKeyReleased

    private void btnReplaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReplaceAllActionPerformed
        String find = txtReFind.getText();
        String rep = txtReplace.getText();
        boolean isExisted = txtContent.getText().contains(find);
        if (!isExisted) { //optional, notepad khong thong bao not found khi an replace all
            replaceDialog.setAlwaysOnTop(false);
            JOptionPane.showMessageDialog(this, "Cannot find \"" + find + "\"", "My Text Editor", JOptionPane.INFORMATION_MESSAGE);
            replaceDialog.setAlwaysOnTop(true);
        } else {
            txtContent.setText(txtContent.getText().replace(find, rep));
        }
    }//GEN-LAST:event_btnReplaceAllActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NotepadJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NotepadJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NotepadJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NotepadJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NotepadJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelFind;
    private javax.swing.JButton btnCancelReplace;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnFindNext;
    private javax.swing.ButtonGroup btnGroupDirection;
    private javax.swing.JButton btnReplace;
    private javax.swing.JButton btnReplaceAll;
    private javax.swing.JDialog findDialog;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFind;
    private javax.swing.JLabel lblReFind;
    private javax.swing.JLabel lblReplace;
    private javax.swing.JMenuItem mChangeFont;
    private javax.swing.JMenuItem mCopy;
    private javax.swing.JMenuItem mCut;
    private javax.swing.JMenuItem mExit;
    private javax.swing.JMenuItem mFind;
    private javax.swing.JMenuItem mNew;
    private javax.swing.JMenuItem mOpen;
    private javax.swing.JMenuItem mPaste;
    private javax.swing.JMenuItem mRedo;
    private javax.swing.JMenuItem mReplace;
    private javax.swing.JMenuItem mSave;
    private javax.swing.JMenuItem mSaveAs;
    private javax.swing.JMenuItem mSelectAll;
    private javax.swing.JMenuItem mUndo;
    private javax.swing.JMenu menuEdit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JPanel pnlFindDirection;
    private javax.swing.JRadioButton rbtnDown;
    private javax.swing.JRadioButton rbtnUp;
    private javax.swing.JDialog replaceDialog;
    private javax.swing.JTextArea txtContent;
    private javax.swing.JTextField txtFind;
    private javax.swing.JTextField txtReFind;
    private javax.swing.JTextField txtReplace;
    // End of variables declaration//GEN-END:variables
}
