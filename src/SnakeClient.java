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
    /** 其他边框的宽度 **/
    public static final int BORDER_WIDTH = 16;
    /** 初始化贪吃蛇移动的方向 */
    Direction dir = Direction.LEFT;
    /**
     * @Fields field:field:{todo}(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 1L;
    /** 游戏客户端的宽度 */
    private static final int GAME_WIDTH = 400 + BORDER_WIDTH * 2;
    /** 游戏客户端的高度 */
    private static final int GAME_HEIGHT = 400 + SCORE_AREA + BORDER_WIDTH;
    /** 贪吃蛇身上的蛇肉集合 */
    List<Food> snake = new ArrayList<>();
    /** 屏幕上的食物，可以吃的  */
    Food foodForEat = null;
    /** 随机生成的食物的x坐标 */
    Random xRandom = new Random();
    /** 随机生成的食物的y坐标*/
    Random yRandom = new Random();
    /** 线程池的实例 */
    ThreadPoolService threadpool = null;
    /** 贪吃蛇自动移动线程的实例 */
    SnakeRun run = null;
    /** 画面自动刷新线程的实例 */
    Refresh fresh = null;
    /** 记录蛇头 **/
    Food snakeHead = null;
    
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
        
        snakeHead = snake.get(3);
        
        run = new SnakeRun();
        fresh = new Refresh();
        // 启动移动线程
        ThreadPoolService.getInstance().execute(run);
        // 启动刷新线程
        ThreadPoolService.getInstance().execute(fresh);
        
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
        // 设置画笔的颜色为橘色
        gOfBackScreen.setColor(Color.PINK);
        // 画出游戏区
        gOfBackScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gOfBackScreen.setColor(Color.LIGHT_GRAY);
        gOfBackScreen.fillRect(BORDER_WIDTH, SCORE_AREA, 400, 400);
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
    }
    
    /**
    * @Title: snakeMove
    * @Description: 通过删除蛇尾，添加蛇头的方式实现贪吃蛇的移动
    * @param     参数 
    * @return void    返回类型
    * @throws
     */
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
        snakeHead = new Food(x, y, this);
        collisionDetection();
        snake.add(snakeHead);
        // 当贪吃蛇吃到食物后，移动时不删除蛇尾
        if (eat != true) {
            snake.remove(0);
        }
    }
    
    /**
    * @Title: produceFood
    * @Description: 随机在屏幕上生成一个食物
    * @param     参数 
    * @return void    返回类型
    * @throws
     */
    public void produceFood() {
        Long timeStart = System.currentTimeMillis();
        boolean produce = false;
        while (produce == false) {
            int xFood = -1;
            int yFood = -1;
            int x = -1;
            int y = -1;
            while (xFood == -1 || yFood == -1) {
                if (xFood == -1) {
                    x = xRandom.nextInt(GAME_WIDTH - BORDER_WIDTH) + BORDER_WIDTH;
                }
                
                if (yFood == -1) {
                    y = yRandom.nextInt(GAME_HEIGHT - BORDER_WIDTH) + SCORE_AREA;
                }
                
                if (xFood == -1 && ((x - BORDER_WIDTH) % 8) == 0) {
                    xFood = x;
                }
                
                if (yFood == -1 && ((y - SCORE_AREA) % 8) == 0) {
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
        Long timeEnd = System.currentTimeMillis();
        System.out.println("produce:" + (timeEnd - timeStart) + "ms");
    }
    
    public void collisionDetection() {
        // 蛇头与蛇身碰撞检测
        for (int i=0; i<snake.size() - 1; i++) {
            Food foodOnSnake = snake.get(i);
            if (foodOnSnake.equals(snakeHead) != true && foodOnSnake.getRectangle()
                    .intersects(snakeHead.getRectangle())) {
                // 停止画面刷新线程
                run.stopRun();
System.out.println("Stop");
            }
        }
        
        // 蛇头与边框碰撞检测
        if (run != null) {
            if (snakeHead.x < BORDER_WIDTH || snakeHead.x > (GAME_WIDTH - BORDER_WIDTH) || snakeHead.y < SCORE_AREA
                    || snakeHead.y > (GAME_HEIGHT - BORDER_WIDTH)) {
                // 停止画面刷新线程
                run.stopRun();
System.out.println("Stop");           
            }
        }
    }
    
    /**
    * @Title: eatFood
    * @Description: 判断移动的过程中是否触碰到了食物
    * @param @return    参数 
    * @return boolean    返回类型
    * @throws
     */
    public boolean eatFood() {
        boolean eat = false;
        // 蛇头与食物碰撞
        if (foodForEat != null && snakeHead.getRectangle().intersects(foodForEat.getRectangle())) {
                eat = true;
                // 随机在屏幕上生成一个食物
                produceFood();
        }
        
        return eat;
    }
    
    /**
     * @ProjectName:  [GluttonousSnake] 
     * @Package:      [.SnakeClient.java]  
     * @ClassName:    [SnakeRun]   
     * @Description:  [实现贪吃蛇客户端画面的repaint线程]   
     * @Author:       [Guiyajun]   
     * @CreateDate:   [2019年11月7日 下午4:34:50]   
     * @UpdateUser:   [Guiyajun]   
     * @UpdateDate:   [2019年11月7日 下午4:34:50]   
     * @UpdateRemark: [说明本次修改内容]  
     * @Version:      [v1.0]
     */
    private class SnakeRun implements Runnable {
        boolean runStat = true;
        @Override
        public void run() {
            while(runStat) {
                snakeMove();
                try {
                    // 每刷新一次等待的时间间隔，ms是单位
                    Thread.sleep(150);   
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
        * @Title: stopRun
        * @Description: 当贪吃蛇撞墙或者撞到自己，可以调用该方法停止刷新
        * @param     参数 
        * @return void    返回类型
        * @throws
         */
        public void stopRun() {
            runStat = false;
        }
    }
    
    private class Refresh implements Runnable {
        boolean runStat = true;
        @Override
        public void run() {
            while(runStat) {
                // 调用重画方法，重画方法会先调用update方法，再调用paint方法
                repaint();      
                try {
                    // 每刷新一次等待的时间间隔，ms是单位
                    Thread.sleep(50);   
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
        * @Title: stopRun
        * @Description: 当贪吃蛇撞墙或者撞到自己，可以调用该方法停止刷新
        * @param     参数 
        * @return void    返回类型
        * @throws
         */
        public void stopRun() {
            runStat = false;
        }
    }
}
