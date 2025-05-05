import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class QuickSortVisualizer extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BAR_WIDTH = 10;
    private static final int NUMBER_OF_BARS = WIDTH / BAR_WIDTH;

    private int[] array;
    private SortPanel sortPanel;
    private JButton startButton;
    private JButton resetButton;
    private JSlider speedSlider;
    private volatile boolean sorting = false;
    private volatile boolean paused = false;

    public QuickSortVisualizer() {
        setTitle("Quick Sort Visualizer");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        array = new int[NUMBER_OF_BARS];
        initializeArray();

        sortPanel = new SortPanel();

        JPanel controlPanel = new JPanel();
        startButton = new JButton("開始排序");
        resetButton = new JButton("重置數組");
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
        speedSlider.setMajorTickSpacing(20);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!sorting) {
                    sorting = true;
                    paused = false;
                    startButton.setText("暫停");
                    resetButton.setEnabled(false);
                    new SortingThread().start();
                } else if (paused) {
                    paused = false;
                    startButton.setText("暫停");
                    synchronized (sortPanel) {
                        sortPanel.notify();
                    }
                } else {
                    paused = true;
                    startButton.setText("繼續");
                }
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeArray();
                sortPanel.repaint();
            }
        });

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(new JLabel("速度:"));
        controlPanel.add(speedSlider);

        setLayout(new BorderLayout());
        add(sortPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void initializeArray() {
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(HEIGHT - 150) + 10;
        }
    }

    private class SortPanel extends JPanel {
        private int pivotIndex = -1;
        private int currentI = -1;
        private int currentJ = -1;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 清除背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // 繪製柱狀圖
            for (int i = 0; i < array.length; i++) {
                if (i == pivotIndex) {
                    g.setColor(Color.RED);
                } else if (i == currentI) {
                    g.setColor(Color.GREEN);
                } else if (i == currentJ) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(i * BAR_WIDTH, getHeight() - array[i], BAR_WIDTH - 1, array[i]);
            }
        }

        public void updateIndices(int pivot, int i, int j) {
            this.pivotIndex = pivot;
            this.currentI = i;
            this.currentJ = j;
            repaint();
        }
    }

    private class SortingThread extends Thread {
        @Override
        public void run() {
            try {
                quickSort(0, array.length - 1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 確保在排序結束時重置UI
                SwingUtilities.invokeLater(() -> {
                    sorting = false;
                    paused = false;
                    startButton.setText("開始排序");
                    resetButton.setEnabled(true);
                    sortPanel.updateIndices(-1, -1, -1);
                });
            }
        }

        private void quickSort(int low, int high) {
            if (low < high) {
                int pivotIndex = partition(low, high);
                quickSort(low, pivotIndex - 1);
                quickSort(pivotIndex + 1, high);
            }
        }

        private int partition(int low, int high) {
            int pivot = array[high];
            int i = low - 1;

            sortPanel.updateIndices(high, i, low);
            delay();

            for (int j = low; j < high; j++) {
                sortPanel.updateIndices(high, i, j);
                delay();

                if (array[j] <= pivot) {
                    i++;
                    swap(i, j);
                    sortPanel.updateIndices(high, i, j);
                    delay();
                }
            }

            swap(i + 1, high);
            sortPanel.updateIndices(-1, i + 1, -1);
            delay();

            return i + 1;
        }

        private void swap(int i, int j) {
            if (i != j) {
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
                sortPanel.repaint();
                delay();
            }
        }

        private void delay() {
            try {
                Thread.sleep(Math.max(5, 101 - speedSlider.getValue()));
                if (paused) {
                    synchronized (sortPanel) {
                        while (paused) {
                            sortPanel.wait();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new QuickSortVisualizer().setVisible(true);
            }
        });
    }
}
