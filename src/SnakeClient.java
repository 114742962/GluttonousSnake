import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
* @Title: Snake.java
* @Package 
* @Description: TODO(用一句话描述该文件做什么)
* @author Administrator
* @date 2019年11月4日
* @version V1.0
*/

/**
 * @ProjectName:  [GluttonousSnake] 
 * @Package:      [.Snake.java]  
 * @ClassName:    [Snake]   
 * @Description:  [一句话描述该类的功能]   
 * @Author:       [桂亚君]   
 * @CreateDate:   [2019年11月4日 下午9:33:34]   
 * @UpdateUser:   [桂亚君]   
 * @UpdateDate:   [2019年11月4日 下午9:33:34]   
 * @UpdateRemark: [说明本次修改内容]  
 * @Version:      [v1.0]
*/
public class SnakeClient extends Frame{
    int x = 300;
    int y = 300;
    int snakeStartLenth = 4;
    /** 定义一个虚拟屏幕，目的是双缓冲，先把图片画到虚拟屏幕上 */
    Image backScreen = null;
    /** 记分区的高度，宽度与游戏客户端宽度一致，这里不做申明 */ 
    public static final int SCORE_AREA = 60;
    Direction dir = Direction.LEFT;
    int xOld;
    int yOld;
    /**
     * @Fields field:field:{todo}(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 1L;
    private static final int GAME_WIDTH = 400;
    private static final int GAME_HEIGHT = 460;
    List<Food> snake = new ArrayList<>();
    
    public static void main(String[] args) {
        SnakeClient snakeClient = new SnakeClient();
        snakeClient.launchFrame();
    }
    
    private void launchFrame() {
        setTitle("GluttonousSnake");
        setBounds(400, 100, GAME_WIDTH, GAME_HEIGHT);
        setBackground(Color.LIGHT_GRAY);
        setResizable(false);
        setVisible(true);
        
        for (int i=0; i<snakeStartLenth; i++) {
            snake.add(new Food(x + Food.WIDTH * (3 - i), y, this));
        }
        
        // 启动画面刷新线程池
        ThreadPoolService.getInstance().execute(new SnakeRun()); 
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                System.exit(0);
            }
            
        });
        
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                        dir = Direction.UP;
                        break;
                    case KeyEvent.VK_LEFT:
                        dir = Direction.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                        dir = Direction.RIGHT;
                        break;                    
                    case KeyEvent.VK_DOWN:
                        dir = Direction.DOWN;
                        break;
                    default:
                        break;
                }
            }
        });
    }
    
    @Override
    public void paint(Graphics g) {
        for (int i=0; i<snake.size(); i++) {
            Food foodOnSnake = snake.get(i);
            foodOnSnake.draw(g);
        }
    }
    
    @Override
    public void update(Graphics g) {
        // 在一个虚拟屏幕上画图，画完后再显示到真实屏幕上，利用双缓冲解决界面闪烁问题
        if(backScreen == null) {
            backScreen = this.createImage(GAME_WIDTH, GAME_HEIGHT);
        }
        
        // 获取虚拟屏幕的画笔
        Graphics gOfBackScreen = backScreen.getGraphics();
        // 获取画笔的初始颜色
        Color c = gOfBackScreen.getColor();
        // 设置画笔的颜色为粉色
        gOfBackScreen.setColor(Color.PINK);
        // 画出记分区
        gOfBackScreen.fillRect(0, 0, GAME_WIDTH, SCORE_AREA);
        // 设置画笔的颜色为橘色
        gOfBackScreen.setColor(Color.LIGHT_GRAY);
        // 画出游戏区
        gOfBackScreen.fillRect(0, SCORE_AREA, GAME_WIDTH, GAME_HEIGHT - SCORE_AREA);
        // 设置画笔的颜色为黑色
        gOfBackScreen.setColor(Color.BLACK);
        // 记分区画出snake长度
        gOfBackScreen.drawString("SnakeLength: " + snake.size(), 30, 45);
        // 使用虚拟屏画笔画出屏幕上的元素
        paint(gOfBackScreen);
        // 使用真实屏幕画笔将虚拟屏画出
        g.drawImage(backScreen, 0, 0, null);
        // 还原虚拟屏画笔的初始颜色
        gOfBackScreen.setColor(c);
        
        snakeMove(dir);
    }
    
    public void snakeMove(Direction dir) {
        
        if (dir == null) {
            return;
        }
        
        switch (dir) {
            case UP:
                y -= Food.HEIGHT;
                break;
            case DOWN:
                y += Food.HEIGHT;
                break;
            case LEFT:
                x -= Food.WIDTH;
                break;
            case RIGHT:
                x += Food.WIDTH;
                break;
            default:
                break;
        }
        
        snake.add(new Food(x, y, this));
        snake.remove(0);
    }
    
    private class SnakeRun implements Runnable {
        @Override
        public void run() {
            while(true) {
                // 调用重画方法，重画方法会先调用update方法，再调用paint方法
                repaint();      
                try {
                    // 每刷新一次等待30ms
                    Thread.sleep(100);   
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
