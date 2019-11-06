import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    int x = 280;
    int y = 340;
    int snakeStartLenth = 4;
    /** 定义一个虚拟屏幕，目的是双缓冲，先把图片画到虚拟屏幕上 */
    Image backScreen = null;
    /** 记分区的高度，宽度与游戏客户端宽度一致，这里不做申明 */ 
    public static final int SCORE_AREA = 60;
    Direction dir = Direction.LEFT;
    /**
     * @Fields field:field:{todo}(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 1L;
    private static final int GAME_WIDTH = 400;
    private static final int GAME_HEIGHT = 460;
    List<Food> snake = new ArrayList<>();
    Food foodForEat = null;
    Random xRandom = new Random();
    Random yRandom = new Random();
    ThreadPoolService threadpool = null;
    SnakeRun run = new SnakeRun();
    Thread thread = null;
    Label label = new Label("game over!");
    
    public static void main(String[] args) {
        SnakeClient snakeClient = new SnakeClient();
        snakeClient.launchFrame();
    }
    
    private void launchFrame() {
        setExtendedState(0);
        setTitle("GluttonousSnake");
        setBounds(400, 100, GAME_WIDTH, GAME_HEIGHT);
        setBackground(Color.LIGHT_GRAY);
        setResizable(false);
        label.setBounds(200, 200, 10, 10);
        label.setVisible(false);
        setVisible(true);
        
        for (int i=0; i<snakeStartLenth; i++) {
            snake.add(new Food(x + Food.WIDTH * (3 - i), y, this));
        }
        
        // 启动画面刷新线程池
//        threadpool = new ThreadPoolService();
//        ThreadPoolService.getInstance();
//        threadpool.execute(run);
        run = new SnakeRun();
        thread = new Thread(run);
        thread.start();
        
        produceFood();
        
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
        // 画出贪吃蛇
        for (int i=0; i<snake.size(); i++) {
            Food foodOnSnake = snake.get(i);
            foodOnSnake.draw(g);
        }
        
        if (foodForEat != null) {
            foodForEat.draw(g);
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
        gOfBackScreen.setColor(Color.PINK);
        // 画出游戏区
        gOfBackScreen.fillRect(0, SCORE_AREA, GAME_WIDTH, GAME_HEIGHT - SCORE_AREA);
        gOfBackScreen.setColor(Color.LIGHT_GRAY);
        gOfBackScreen.fillRect(8, SCORE_AREA + 8, GAME_WIDTH - 16, GAME_HEIGHT - SCORE_AREA - 16);
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
        
        snakeMove();
        collisionDetection();
    }
    
    public void snakeMove() {
        boolean eat = false;
        
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
        
        eat = eatFood();
        
        snake.add(new Food(x, y, this));
        if (eat != true) {
            snake.remove(0);
        }
    }
    
    public void produceFood() {
        boolean produce = false;
        // 随机在屏幕上生成一个食物
        while (produce == false) {
System.out.println("produce");
            int xFood = -1;
            int yFood = -1;
            int x = -1;
            int y = -1;
            while (xFood == -1 || yFood == -1) {
                if (xFood == -1) {
                    x = xRandom.nextInt(GAME_WIDTH - 16) + 8;
                }
                
                if (yFood == -1) {
                    y = yRandom.nextInt(GAME_HEIGHT - 68 - 8) + 68;
                }
                
                if (xFood == -1 && ((x - 8) % 8) == 0) {
                    xFood = x;
                }
                
                if (yFood == -1 && ((y - 68) % 8) == 0) {
                    yFood = y;
                }
            }
            
            Rectangle foodRectangle = new Rectangle(xFood, yFood, Food.WIDTH, Food.HEIGHT);
            
            for (int i=0; i<snake.size(); i++) {
                Food foodOnSnake = snake.get(i);
                if ((foodRectangle.intersects(foodOnSnake.getRectangle())) == true) {
                    break;
                }
                
                if(i == (snake.size() - 1)) {
                    produce = true;
                    foodForEat = new Food(xFood, yFood, this);
                }
            }
        }
    }
    
    public void collisionDetection() {
        // 蛇头与蛇身碰撞检测
        for (int i=1; i<snake.size(); i++) {
            Food foodOnSnake = snake.get(i);
            if (foodOnSnake.getRectangle().intersects(snake.get(0).getRectangle())) {
                // 停止画面刷新线程
                run.stopRun();
System.out.println("STOP");
            }
        }
        
        // 蛇头与边框碰撞检测 8, SCORE_AREA, GAME_WIDTH - 16, GAME_HEIGHT - SCORE_AREA - 8
        Food food = snake.get(0);
        if (food.x <= 7 || food.x >= (GAME_WIDTH - 8) || food.y <= SCORE_AREA - 1 || food.y >= (GAME_HEIGHT - 8)) {
            // 停止画面刷新线程
            run.stopRun();
System.out.println("STOP");           
        }
    }
    
    public boolean eatFood() {
        boolean eat = false;
        // 蛇头与食物碰撞
        if (foodForEat != null && snake.get(0).getRectangle().intersects(foodForEat.getRectangle())) {
                eat = true;
                // 随机在屏幕上生成一个食物
                foodForEat = null;
                produceFood();
        }
        
        return eat;
    }
    
    private class SnakeRun implements Runnable {
        boolean runStat = true;
        @Override
        public void run() {
            while(runStat) {
                // 调用重画方法，重画方法会先调用update方法，再调用paint方法
                repaint();      
                try {
                    // 每刷新一次等待30ms
                    Thread.sleep(140);   
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void stopRun() {
            runStat = false;
        }
    }
}
