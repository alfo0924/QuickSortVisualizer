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
    private boolean sorting = false;
    private boolean paused = false;

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
            array[i] = random.nextInt(HEIGHT - 100) + 10;
        }
    }

    private class SortPanel extends JPanel {
        private int pivotIndex = -1;
        private int currentI = -1;
        private int currentJ = -1;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
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
                g.fillRect(i * BAR_WIDTH, HEIGHT - array[i] - 100, BAR_WIDTH - 1, array[i]);
            }
        }

        public void setPivotIndex(int index) {
            this.pivotIndex = index;
        }

        public void setCurrentI(int index) {
            this.currentI = index;
        }

        public void setCurrentJ(int index) {
            this.currentJ = index;
        }
    }

    private class SortingThread extends Thread {
        @Override
        public void run() {
            quickSort(0, array.length - 1);
            sorting = false;
            paused = false;
            startButton.setText("開始排序");
            resetButton.setEnabled(true);
        }

        private void quickSort(int low, int high) {
            if (low < high) {
                int pivotIndex = partition(low, high);
                quickSort(low, pivotIndex - 1);
                quickSort(pivotIndex + 1, high);
            }
        }

        private int partition(int low, int high) {
            sortPanel.setPivotIndex(high);
            int pivot = array[high];
            int i = low - 1;

            sortPanel.setCurrentI(i);

            for (int j = low; j < high; j++) {
                sortPanel.setCurrentJ(j);
                delay();

                if (array[j] < pivot) {
                    i++;
                    sortPanel.setCurrentI(i);
                    delay();

                    swap(i, j);
                }
            }

            swap(i + 1, high);
            sortPanel.setPivotIndex(-1);
            sortPanel.setCurrentI(-1);
            sortPanel.setCurrentJ(-1);
            return i + 1;
        }

        private void swap(int i, int j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            sortPanel.repaint();
            delay();
        }

        private void delay() {
            try {
                Thread.sleep(101 - speedSlider.getValue());
                if (paused) {
                    synchronized (sortPanel) {
                        sortPanel.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
