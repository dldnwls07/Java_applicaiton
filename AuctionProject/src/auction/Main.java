package auction;

import auction.file.AppPaths;
import auction.gui.MainFrame;
import auction.service.AuctionException;
import auction.service.AuctionService;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;

/** 사용자 이름을 입력받고 메인 Swing 창을 시작한다. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { startApplication(); }
        });
    }

    private static void startApplication() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        AuctionService service = new AuctionService();
        try {
            service.initializeFiles();
            handleStaleLock();
        } catch (AuctionException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "시작 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userName = askUserName();
        if (userName == null) return;
        try {
            service.registerUser(userName);
        } catch (AuctionException e) {
            JOptionPane.showMessageDialog(null, "사용자 목록 저장은 건너뜁니다.\n" + e.getMessage(),
                    "안내", JOptionPane.WARNING_MESSAGE);
        }
        new MainFrame(service, userName).setVisible(true);
    }

    private static String askUserName() {
        while (true) {
            String name = JOptionPane.showInputDialog(null, "사용자 이름을 입력해 주세요.",
                    "교내 경매 로그인", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return null;
            name = name.trim().replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
            if (!name.isEmpty()) return name;
            JOptionPane.showMessageDialog(null, "사용자 이름은 비워 둘 수 없습니다.");
        }
    }

    /** 비정상 종료로 오래 남은 잠금 파일만 사용자 확인 후 삭제한다. */
    private static void handleStaleLock() {
        try {
            if (!Files.exists(AppPaths.LOCK)) return;
            Instant modified = Files.getLastModifiedTime(AppPaths.LOCK).toInstant();
            long age = Duration.between(modified, Instant.now()).getSeconds();
            if (age < 30) return;
            int answer = JOptionPane.showConfirmDialog(null,
                    "이전 실행에서 남은 것으로 보이는 잠금 파일이 있습니다.\n삭제하고 계속할까요?",
                    "잠금 파일 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) Files.deleteIfExists(AppPaths.LOCK);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
