import java.awt.Color;
import java.awt.Graphics;

/**
* @Title: Food.java
* @Package 
* @Description: TODO(用一句话描述该文件做什么)
* @author Administrator
* @date 2019年11月4日
* @version V1.0
*/

/**
 * @ProjectName:  [GluttonousSnake] 
 * @Package:      [.Food.java]  
 * @ClassName:    [Food]   
 * @Description:  [一句话描述该类的功能]   
 * @Author:       [桂亚君]   
 * @CreateDate:   [2019年11月4日 下午9:41:07]   
 * @UpdateUser:   [桂亚君]   
 * @UpdateDate:   [2019年11月4日 下午9:41:07]   
 * @UpdateRemark: [说明本次修改内容]  
 * @Version:      [v1.0]
*/
public class Food {
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    int x;
    int y;
    Direction dir;
    
    private SnakeClient snakeClient = null;
    
    Food(int x, int y, SnakeClient snakeClient) {
        this.x = x;
        this.y = y;
        this.snakeClient = snakeClient;
    }
    
    Food(int x, int y, Direction dir, SnakeClient snakeClient) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.snakeClient = snakeClient;
    }
    
    public void draw(Graphics g) {
        Color c = g.getColor();
        g.setColor(Color.WHITE);
        g.fillRect(x, y, WIDTH, HEIGHT);
    }
    
    public void move(Direction dir) {
        
        if (dir == null) {
            return;
        }
        
        switch (dir) {
            case UP:
                y -= HEIGHT;
                break;
            case DOWN:
                y += HEIGHT;
                break;
            case LEFT:
                x -= WIDTH;
                break;
            case RIGHT:
                x += WIDTH;
                break;
            default:
                break;
        }
    }

}
