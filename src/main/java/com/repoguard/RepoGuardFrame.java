package com.repoguard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.Map;
import java.util.HashMap;

public final class RepoGuardFrame extends JFrame {
    private static final Color NAVY = new Color(15, 23, 42), BLUE = new Color(37, 99, 235), BG = new Color(245, 247, 251);
    private final JTextField pathField = new JTextField(System.getProperty("user.dir"));
    private final JLabel status = new JLabel("Ready to protect your next commit");
    private final JLabel summary = new JLabel("No scan yet");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Severity", "Rule", "File", "Line", "Finding"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);
    private final JButton scanButton = new JButton("Scan repository");
    private final Map<String, JLabel> metrics = new HashMap<>();

    public RepoGuardFrame() {
        super("RepoGuard | Secure your commits");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); setSize(1120, 720); setMinimumSize(new Dimension(900, 600)); setLocationRelativeTo(null);
        getContentPane().setBackground(BG); setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH); add(content(), BorderLayout.CENTER); add(footer(), BorderLayout.SOUTH);
        scanButton.addActionListener(e -> scan());
    }
    private JComponent header() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(NAVY); p.setBorder(new EmptyBorder(20, 28, 20, 28));
        JLabel brand = new JLabel("RG  RepoGuard"); brand.setForeground(Color.WHITE); brand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitle = new JLabel("SECURE COMMIT GATE  /  LOCAL FIRST"); subtitle.setForeground(new Color(148, 163, 184)); subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel texts = new JPanel(new GridLayout(2,1,0,4)); texts.setOpaque(false); texts.add(brand); texts.add(subtitle); p.add(texts, BorderLayout.WEST);
        JLabel chip = new JLabel("  ●  PROTECTION ACTIVE  "); chip.setOpaque(true); chip.setBackground(new Color(20, 83, 45)); chip.setForeground(new Color(187, 247, 208)); chip.setFont(new Font("Segoe UI", Font.BOLD, 11)); p.add(chip, BorderLayout.EAST); return p;
    }
    private JComponent content() {
        JPanel root = new JPanel(new BorderLayout(0, 18)); root.setBackground(BG); root.setBorder(new EmptyBorder(24,28,20,28));
        JPanel top = new JPanel(new BorderLayout(0,14)); top.setOpaque(false);
        JLabel title = new JLabel("Repository security scan"); title.setFont(new Font("Segoe UI", Font.BOLD, 23)); title.setForeground(new Color(30,41,59));
        JLabel desc = new JLabel("Catch credentials and risky secrets before they leave your machine."); desc.setFont(new Font("Segoe UI", Font.PLAIN, 14)); desc.setForeground(new Color(100,116,139));
        JPanel words = new JPanel(new GridLayout(2,1,0,5)); words.setOpaque(false); words.add(title); words.add(desc); top.add(words, BorderLayout.NORTH);
        JPanel picker = new JPanel(new BorderLayout(10,0)); picker.setOpaque(false); pathField.setFont(new Font("Segoe UI",Font.PLAIN,14)); pathField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203,213,225)),new EmptyBorder(10,12,10,12)));
        JButton browse = new JButton("Browse"); browse.addActionListener(e -> { JFileChooser c = new JFileChooser(pathField.getText()); c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); if(c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) pathField.setText(c.getSelectedFile().getAbsolutePath()); });
        scanButton.setBackground(BLUE); scanButton.setForeground(Color.WHITE); scanButton.setFocusPainted(false); scanButton.setFont(new Font("Segoe UI",Font.BOLD,13));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actions.setOpaque(false); actions.add(browse); actions.add(scanButton);
        picker.add(pathField,BorderLayout.CENTER); picker.add(actions,BorderLayout.EAST); top.add(picker,BorderLayout.SOUTH);
        root.add(top,BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(0,12)); middle.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1,4,12,0)); cards.setOpaque(false); cards.add(card("FILES SCANNED","Ready","Checks source and<br>config files")); cards.add(card("CRITICAL","0","Private keys and<br>provider tokens")); cards.add(card("HIGH","0","Hard-coded<br>credentials")); cards.add(card("MEDIUM","0","Credential-like assignments")); middle.add(cards,BorderLayout.NORTH);
        table.setRowHeight(30); table.setFont(new Font("Segoe UI",Font.PLAIN,13)); table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12)); table.getTableHeader().setBackground(new Color(241,245,249)); table.setFillsViewportHeight(true); table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(new Color(226,232,240))); middle.add(scroll,BorderLayout.CENTER);
        root.add(middle,BorderLayout.CENTER); return root;
    }
    private JPanel card(String name,String value,String note) { JPanel c = new JPanel(new GridLayout(3,1,0,5)); c.setPreferredSize(new Dimension(150,110)); c.setBackground(Color.WHITE); c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226,232,240)),new EmptyBorder(12,14,12,14))); JLabel a=new JLabel(name);a.setForeground(new Color(100,116,139));a.setFont(new Font("Segoe UI",Font.BOLD,10)); JLabel b=new JLabel(value);b.setFont(new Font("Segoe UI",Font.BOLD,22));b.setForeground(new Color(30,41,59));metrics.put(name,b);JLabel d=new JLabel("<html><body style='width:90px'>"+note.replace("<br>","<br>")+"</body></html>");d.setForeground(new Color(100,116,139));d.setFont(new Font("Segoe UI",Font.PLAIN,10));c.add(a);c.add(b);c.add(d);return c; }
    private JComponent footer() { JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(226,232,240)),new EmptyBorder(10,28,10,28)));status.setFont(new Font("Segoe UI",Font.PLAIN,12)); status.setForeground(new Color(71,85,105));summary.setFont(new Font("Segoe UI",Font.PLAIN,12));summary.setForeground(new Color(100,116,139));p.add(status,BorderLayout.WEST);p.add(summary,BorderLayout.EAST);return p; }
    private void scan() {
        Path root; try { root=Path.of(pathField.getText().trim()); } catch(Exception e) { status.setText("Enter a valid folder path."); return; }
        scanButton.setEnabled(false); status.setText("Scanning repository securely..."); model.setRowCount(0);
        new SwingWorker<ScanReport,Void>() { protected ScanReport doInBackground() throws Exception { return new SecretScanner().scan(root); }
            protected void done() { scanButton.setEnabled(true); try { ScanReport r=get(); setMetric("FILES SCANNED",Integer.toString(r.filesScanned())); setMetric("CRITICAL",Long.toString(r.criticalCount()));setMetric("HIGH",Long.toString(r.highCount()));setMetric("MEDIUM",Long.toString(r.mediumCount())); for(Finding f:r.findings()) model.addRow(new Object[]{f.severity(),f.rule(),r.root().relativize(f.file()),f.line(),f.message()}); status.setText(r.isClean()?"✓  No findings. This repository is ready for review.":"⚠  Findings detected. Review and resolve before committing.");summary.setText(r.findings().size()+" finding(s)  •  "+r.durationMillis()+" ms  •  "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); } catch(InterruptedException e){Thread.currentThread().interrupt();status.setText("Scan interrupted.");}catch(ExecutionException e){status.setText("Scan failed: "+e.getCause().getMessage());} }
        }.execute();
    }
    private void setMetric(String key,String value) { JLabel metric=metrics.get(key); if(metric!=null) metric.setText(value); }
}


