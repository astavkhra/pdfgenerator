import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VirtualRealityWorld extends JPanel implements KeyListener, MouseMotionListener, MouseListener {
    // Camera properties
    private double cameraX = 0, cameraY = 5, cameraZ = 50;
    private double cameraYaw = 0, cameraPitch = 0;
    private double velocityX = 0, velocityZ = 0, velocityY = 0;
    
    // Movement flags
    private boolean moveForward, moveBackward, moveLeft, moveRight, sprint;
    private boolean onGround = true;
    
    // Constants
    private double MOVE_SPEED = 0.5;
    private static final double SPRINT_MULTIPLIER = 2.0;
    private static final double GRAVITY = 0.3;
    private static final double JUMP_STRENGTH = 8.0;
    private static final double MOUSE_SENSITIVITY = 0.003;
    
    // Objects in the scene
    private List<WorldObject> objects = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private WorldObject targetedObject = null;
    
    // Mouse control
    private Point lastMousePos;
    private boolean mouseCaptured = false;
    private Robot robot;
    
    // UI feedback
    private String statusMessage = "Welcome! Click to start";
    private int messageTimer = 0;
    private int score = 0;
    private double fps = 0;
    private long lastFrameTime = System.nanoTime();
    
    // Time of day
    private float timeOfDay = 0;
    private Color skyColor;
    
    public VirtualRealityWorld() {
        setPreferredSize(new Dimension(1400, 900));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        
        initializeWorld();
        
        // Start game loop with better timing
        Timer timer = new Timer(16, e -> {
            update();
            repaint();
            calculateFPS();
        });
        timer.start();
    }
    
    private void calculateFPS() {
        long currentTime = System.nanoTime();
        fps = 1000000000.0 / (currentTime - lastFrameTime);
        lastFrameTime = currentTime;
    }
    
    private void initializeWorld() {
        Random rand = new Random();
        
        // Create diverse buildings with different heights
        objects.add(new Building(-30, 0, -20, 15, 20, 15, new Color(255, 107, 107)));
        objects.add(new Building(30, 0, -20, 12, 28, 12, new Color(78, 205, 196)));
        objects.add(new Building(-30, 0, 20, 18, 16, 18, new Color(255, 230, 109)));
        objects.add(new Building(30, 0, 20, 15, 24, 15, new Color(149, 225, 211)));
        objects.add(new Building(0, 0, -40, 20, 30, 20, new Color(180, 120, 220)));
        
        // Create forest of trees
        for (int i = 0; i < 30; i++) {
            double x = (rand.nextDouble() - 0.5) * 180;
            double z = (rand.nextDouble() - 0.5) * 180;
            if (Math.abs(x) > 25 || Math.abs(z) > 25) {
                objects.add(new Tree(x, 0, z));
            }
        }
        
        // Create collectible spheres
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double radius = 40;
            Color[] colors = {Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK, 
                            Color.YELLOW, Color.GREEN, new Color(255, 100, 255), Color.RED};
            objects.add(new CollectibleSphere(
                Math.cos(angle) * radius, 
                8, 
                Math.sin(angle) * radius, 
                3, 
                colors[i]
            ));
        }
        
        // Add some lamp posts
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 * i) / 6;
            double radius = 60;
            objects.add(new LampPost(
                Math.cos(angle) * radius, 
                0, 
                Math.sin(angle) * radius
            ));
        }
        
        // Add interactive platforms
        objects.add(new Platform(-50, 3, 0, 10, 1, 10, new Color(100, 150, 200)));
        objects.add(new Platform(50, 5, 0, 10, 1, 10, new Color(200, 150, 100)));
        objects.add(new Platform(0, 7, -50, 10, 1, 10, new Color(150, 200, 100)));
    }
    
    private void update() {
        // Update time of day
        timeOfDay += 0.001f;
        if (timeOfDay > 1) timeOfDay = 0;
        updateSkyColor();
        
        // Calculate movement speed
        double currentSpeed = sprint ? MOVE_SPEED * SPRINT_MULTIPLIER : MOVE_SPEED;
        
        // Update velocity based on input
        double moveX = 0, moveZ = 0;
        if (moveForward) moveZ -= currentSpeed;
        if (moveBackward) moveZ += currentSpeed;
        if (moveLeft) moveX -= currentSpeed;
        if (moveRight) moveX += currentSpeed;
        
        // Rotate movement based on camera yaw
        double rotatedX = moveX * Math.cos(cameraYaw) - moveZ * Math.sin(cameraYaw);
        double rotatedZ = moveX * Math.sin(cameraYaw) + moveZ * Math.cos(cameraYaw);
        
        velocityX = rotatedX * 0.9; // Smooth movement
        velocityZ = rotatedZ * 0.9;
        
        // Apply gravity
        if (!onGround) {
            velocityY -= GRAVITY;
        }
        
        // Update position
        cameraX += velocityX;
        cameraZ += velocityZ;
        cameraY += velocityY;
        
        // Ground collision
        if (cameraY <= 5) {
            cameraY = 5;
            velocityY = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        
        // Check for platform collisions
        for (WorldObject obj : objects) {
            if (obj instanceof Platform) {
                Platform platform = (Platform) obj;
                if (cameraX > platform.x - platform.width/2 && 
                    cameraX < platform.x + platform.width/2 &&
                    cameraZ > platform.z - platform.depth/2 && 
                    cameraZ < platform.z + platform.depth/2 &&
                    cameraY > platform.y - 1 && 
                    cameraY < platform.y + platform.height + 2 &&
                    velocityY < 0) {
                    cameraY = platform.y + platform.height + 5;
                    velocityY = 0;
                    onGround = true;
                }
            }
        }
        
        // Update animated objects
        for (WorldObject obj : objects) {
            obj.update();
        }
        
        // Update particles
        particles.removeIf(p -> !p.isAlive());
        for (Particle p : particles) {
            p.update();
        }
        
        // Check what player is looking at
        updateTargetedObject();
        
        // Update message timer
        if (messageTimer > 0) {
            messageTimer--;
        }
    }
    
    private void updateSkyColor() {
        // Cycle through day/night
        float t = timeOfDay;
        if (t < 0.25f) { // Dawn
            skyColor = interpolateColor(new Color(25, 25, 50), new Color(135, 206, 235), t * 4);
        } else if (t < 0.5f) { // Day
            skyColor = new Color(135, 206, 235);
        } else if (t < 0.75f) { // Dusk
            skyColor = interpolateColor(new Color(135, 206, 235), new Color(255, 140, 60), (t - 0.5f) * 4);
        } else { // Night
            skyColor = interpolateColor(new Color(255, 140, 60), new Color(25, 25, 50), (t - 0.75f) * 4);
        }
    }
    
    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, g, b);
    }
    
    private void updateTargetedObject() {
        targetedObject = null;
        double minDist = 50; // Max interaction distance
        
        for (WorldObject obj : objects) {
            if (!(obj instanceof Tree)) { // Trees are not interactive
                double dx = obj.x - cameraX;
                double dy = obj.y - cameraY;
                double dz = obj.z - cameraZ;
                double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
                
                // Check if looking at object (rough raycast)
                double angle = Math.atan2(dz, dx) - cameraYaw + Math.PI/2;
                while (angle > Math.PI) angle -= 2*Math.PI;
                while (angle < -Math.PI) angle += 2*Math.PI;
                
                if (Math.abs(angle) < 0.3 && distance < minDist) {
                    targetedObject = obj;
                    minDist = distance;
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Dynamic sky
        setBackground(skyColor);
        
        // Draw ground with perspective
        drawGround(g2, width, height);
        
        // Sort objects by distance (painter's algorithm)
        objects.sort((a, b) -> {
            double distA = Math.sqrt(Math.pow(a.x - cameraX, 2) + Math.pow(a.z - cameraZ, 2));
            double distB = Math.sqrt(Math.pow(b.x - cameraX, 2) + Math.pow(b.z - cameraZ, 2));
            return Double.compare(distB, distA);
        });
        
        // Draw all objects with highlighting for targeted object
        for (WorldObject obj : objects) {
            boolean isTargeted = (obj == targetedObject);
            obj.draw(g2, width, height, cameraX, cameraY, cameraZ, cameraYaw, cameraPitch, isTargeted);
        }
        
        // Draw particles on top
        for (Particle p : particles) {
            p.draw(g2, width, height);
        }
        
        // Draw enhanced HUD
        drawEnhancedHUD(g2, width, height);
    }
    
    private void drawGround(Graphics2D g2, int width, int height) {
        // Draw ground with gradient
        g2.setColor(new Color(58, 140, 58));
        
        // Draw grid for ground with fade
        for (int x = -100; x <= 100; x += 10) {
            Point2D p1 = project(x, 0, -100, width, height);
            Point2D p2 = project(x, 0, 100, width, height);
            if (p1 != null && p2 != null) {
                double dist = Math.sqrt(Math.pow(x - cameraX, 2) + 10000);
                int alpha = Math.max(0, Math.min(255, (int)(255 - dist * 2)));
                g2.setColor(new Color(48, 130, 48, alpha));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            }
        }
        
        for (int z = -100; z <= 100; z += 10) {
            Point2D p1 = project(-100, 0, z, width, height);
            Point2D p2 = project(100, 0, z, width, height);
            if (p1 != null && p2 != null) {
                double dist = Math.sqrt(Math.pow(z - cameraZ, 2) + 10000);
                int alpha = Math.max(0, Math.min(255, (int)(255 - dist * 2)));
                g2.setColor(new Color(48, 130, 48, alpha));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            }
        }
    }
    
    private void drawEnhancedHUD(Graphics2D g2, int width, int height) {
        // Semi-transparent panel
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 320, 160, 15, 15);
        
        g2.setColor(new Color(255, 255, 255, 230));
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("VR ENVIRONMENT", 25, 35);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.drawString(String.format("Position: %.1f, %.1f, %.1f", cameraX, cameraY, cameraZ), 25, 60);
        g2.drawString(String.format("Score: %d | FPS: %.0f", score, fps), 25, 80);
        
        g2.setColor(new Color(100, 200, 255));
        g2.drawString("Controls:", 25, 105);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("W/A/S/D - Move | Shift - Sprint", 25, 125);
        g2.drawString("Mouse - Look | Space - Jump", 25, 142);
        g2.drawString("E - Interact | R - Reset View", 25, 159);
        
        // Crosshair with interaction indicator
        if (targetedObject != null) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
        }
        g2.drawLine(width/2 - 15, height/2, width/2 + 15, height/2);
        g2.drawLine(width/2, height/2 - 15, width/2, height/2 + 15);
        g2.drawOval(width/2 - 5, height/2 - 5, 10, 10);
        
        // Interaction prompt
        if (targetedObject != null) {
            g2.setColor(new Color(255, 255, 100, 200));
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String prompt = "Press E to interact";
            int promptWidth = g2.getFontMetrics().stringWidth(prompt);
            g2.drawString(prompt, width/2 - promptWidth/2, height/2 + 40);
        }
        
        // Status message
        if (messageTimer > 0) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(width/2 - 200, height - 80, 400, 50, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            int msgWidth = g2.getFontMetrics().stringWidth(statusMessage);
            g2.drawString(statusMessage, width/2 - msgWidth/2, height - 50);
        }
        
        // Mini-map (top right)
        drawMiniMap(g2, width, height);
    }
    
    private void drawMiniMap(Graphics2D g2, int width, int height) {
        int mapSize = 150;
        int mapX = width - mapSize - 20;
        int mapY = 20;
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(mapX, mapY, mapSize, mapSize, 10, 10);
        
        g2.setColor(new Color(50, 150, 50, 150));
        g2.fillRoundRect(mapX + 5, mapY + 5, mapSize - 10, mapSize - 10, 5, 5);
        
        // Draw objects on minimap
        for (WorldObject obj : objects) {
            if (!(obj instanceof Tree)) {
                double relX = (obj.x - cameraX) / 200.0;
                double relZ = (obj.z - cameraZ) / 200.0;
                
                int objX = (int)(mapX + mapSize/2 + relX * (mapSize/2));
                int objZ = (int)(mapY + mapSize/2 + relZ * (mapSize/2));
                
                if (obj instanceof CollectibleSphere) {
                    g2.setColor(obj.color);
                    g2.fillOval(objX - 3, objZ - 3, 6, 6);
                } else {
                    g2.setColor(new Color(200, 200, 200, 150));
                    g2.fillRect(objX - 2, objZ - 2, 4, 4);
                }
            }
        }
        
        // Draw player position
        g2.setColor(Color.RED);
        g2.fillOval(mapX + mapSize/2 - 4, mapY + mapSize/2 - 4, 8, 8);
        
        // Draw direction indicator
        double dirX = Math.sin(cameraYaw) * 15;
        double dirZ = Math.cos(cameraYaw) * 15;
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(mapX + mapSize/2, mapY + mapSize/2, 
                    mapX + mapSize/2 + (int)dirX, mapY + mapSize/2 + (int)dirZ);
    }
    
    private Point2D project(double x, double y, double z, int width, int height) {
        double dx = x - cameraX;
        double dy = y - cameraY;
        double dz = z - cameraZ;
        
        // Rotate around Y axis (yaw)
        double rotX = dx * Math.cos(cameraYaw) + dz * Math.sin(cameraYaw);
        double rotZ = -dx * Math.sin(cameraYaw) + dz * Math.cos(cameraYaw);
        
        // Rotate around X axis (pitch)
        double rotY = dy * Math.cos(cameraPitch) - rotZ * Math.sin(cameraPitch);
        double finalZ = dy * Math.sin(cameraPitch) + rotZ * Math.cos(cameraPitch);
        
        if (finalZ <= 0.1) return null;
        
        double fov = 600;
        double screenX = width/2 + (rotX * fov / finalZ);
        double screenY = height/2 - (rotY * fov / finalZ);
        
        return new Point2D.Double(screenX, screenY);
    }
    
    private void showMessage(String msg) {
        statusMessage = msg;
        messageTimer = 120; // 2 seconds
    }
    
    private void createParticleExplosion(double x, double y, double z, Color color) {
        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            double vx = (rand.nextDouble() - 0.5) * 2;
            double vy = rand.nextDouble() * 2;
            double vz = (rand.nextDouble() - 0.5) * 2;
            particles.add(new Particle(x, y, z, vx, vy, vz, color));
        }
    }
    
    // Key events
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: moveForward = true; break;
            case KeyEvent.VK_S: moveBackward = true; break;
            case KeyEvent.VK_A: moveLeft = true; break;
            case KeyEvent.VK_D: moveRight = true; break;
            case KeyEvent.VK_SHIFT: sprint = true; break;
            case KeyEvent.VK_SPACE:
                if (onGround) {
                    velocityY = JUMP_STRENGTH;
                    onGround = false;
                }
                break;
            case KeyEvent.VK_E:
                if (targetedObject != null) {
                    targetedObject.interact();
                    if (targetedObject instanceof CollectibleSphere) {
                        CollectibleSphere sphere = (CollectibleSphere) targetedObject;
                        if (!sphere.collected) {
                            score += 10;
                            showMessage("Collected! +10 points");
                            createParticleExplosion(sphere.x, sphere.y, sphere.z, sphere.color);
                        }
                    } else if (targetedObject instanceof Building) {
                        showMessage("Building color changed!");
                    }
                }
                break;
            case KeyEvent.VK_R:
                cameraYaw = 0;
                cameraPitch = 0;
                showMessage("View reset");
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: moveForward = false; break;
            case KeyEvent.VK_S: moveBackward = false; break;
            case KeyEvent.VK_A: moveLeft = false; break;
            case KeyEvent.VK_D: moveRight = false; break;
            case KeyEvent.VK_SHIFT: sprint = false; break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // Mouse events with better capture
    @Override
    public void mouseMoved(MouseEvent e) {
        if (mouseCaptured) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            int dx = e.getX() - centerX;
            int dy = e.getY() - centerY;
            
            cameraYaw += dx * MOUSE_SENSITIVITY;
            cameraPitch -= dy * MOUSE_SENSITIVITY;
            
            cameraPitch = Math.max(-Math.PI/2 + 0.1, Math.min(Math.PI/2 - 0.1, cameraPitch));
            
            // Recenter mouse
            if (robot != null && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) {
                Point screenPos = getLocationOnScreen();
                robot.mouseMove(screenPos.x + centerX, screenPos.y + centerY);
            }
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!mouseCaptured) {
            mouseCaptured = true;
            setCursor(getToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "blank"));
            showMessage("Mouse captured! Press ESC to release");
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    // Particle class for effects
    class Particle {
        double x, y, z, vx, vy, vz;
        Color color;
        int life = 60;
        int maxLife = 60;
        
        Particle(double x, double y, double z, double vx, double vy, double vz, Color color) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.color = color;
        }
        
        void update() {
            x += vx;
            y += vy;
            z += vz;
            vy -= 0.05; // Gravity
            life--;
        }
        
        boolean isAlive() {
            return life > 0;
        }
        
        void draw(Graphics2D g2, int width, int height) {
            Point2D p = project(x, y, z, width, height);
            if (p != null) {
                int alpha = (int)(255 * (life / (double)maxLife));
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha)));
                int size = 4;
                g2.fillOval((int)p.getX() - size/2, (int)p.getY() - size/2, size, size);
            }
        }
    }
    
    // Abstract class for world objects
    abstract class WorldObject {
        double x, y, z;
        Color color;
        
        WorldObject(double x, double y, double z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }
        
        abstract void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted);
        void update() {}
        void interact() {}
    }
    
    // Building class with enhanced rendering
    class Building extends WorldObject {
        double width, height, depth;
        boolean isPulsing = false;
        int pulseTimer = 0;
        
        Building(double x, double y, double z, double w, double h, double d, Color color) {
            super(x, y, z, color);
            this.width = w;
            this.height = h;
            this.depth = d;
        }
        
        @Override
        void update() {
            if (pulseTimer > 0) {
                pulseTimer--;
                if (pulseTimer == 0) isPulsing = false;
            }
        }
        
        @Override
        void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted) {
            Color drawColor = color;
            if (highlighted) {
                drawColor = color.brighter();
            }
            if (isPulsing) {
                int pulse = (int)(Math.sin(pulseTimer * 0.3) * 50);
                drawColor = new Color(
                    Math.min(255, drawColor.getRed() + pulse),
                    Math.min(255, drawColor.getGreen() + pulse),
                    Math.min(255, drawColor.getBlue() + pulse)
                );
            }
            
            // Draw front face
            Point2D p1 = project(x - this.width/2, y, z - depth/2, width, height);
            Point2D p2 = project(x + this.width/2, y, z - depth/2, width, height);
            Point2D p3 = project(x + this.width/2, y + this.height, z - depth/2, width, height);
            Point2D p4 = project(x - this.width/2, y + this.height, z - depth/2, width, height);
            
            if (p1 != null && p2 != null && p3 != null && p4 != null) {
                int[] xPoints = {(int)p1.getX(), (int)p2.getX(), (int)p3.getX(), (int)p4.getX()};
                int[] yPoints = {(int)p1.getY(), (int)p2.getY(), (int)p3.getY(), (int)p4.getY()};
                g2.setColor(drawColor);
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.setColor(drawColor.darker().darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(xPoints, yPoints, 4);
            }
            
            // Draw side face
            Point2D s1 = project(x + this.width/2, y, z - depth/2, width, height);
            Point2D s2 = project(x + this.width/2, y, z + depth/2, width, height);
            Point2D s3 = project(x + this.width/2, y + this.height, z + depth/2, width, height);
            Point2D s4 = project(x + this.width/2, y + this.height, z - depth/2, width, height);
            
            if (s1 != null && s2 != null && s3 != null && s4 != null) {
                int[] xPoints = {(int)s1.getX(), (int)s2.getX(), (int)s3.getX(), (int)s4.getX()};
                int[] yPoints = {(int)s1.getY(), (int)s2.getY(), (int)s3.getY(), (int)s4.getY()};
                g2.setColor(drawColor.darker());
                g2.fillPolygon(xPoints, yPoints, 4);
            }
            
            // Draw top face
            Point2D t1 = project(x - this.width/2, y + this.height, z - depth/2, width, height);
            Point2D t2 = project(x + this.width/2, y + this.height, z - depth/2, width, height);
            Point2D t3 = project(x + this.width/2, y + this.height, z + depth/2, width, height);
            Point2D t4 = project(x - this.width/2, y + this.height, z + depth/2, width, height);
            
            if (t1 != null && t2 != null && t3 != null && t4 != null) {
                int[] xPoints = {(int)t1.getX(), (int)t2.getX(), (int)t3.getX(), (int)t4.getX()};
                int[] yPoints = {(int)t1.getY(), (int)t2.getY(), (int)t3.getY(), (int)t4.getY()};
                g2.setColor(drawColor.brighter());
                g2.fillPolygon(xPoints, yPoints, 4);
            }
            
            // Draw windows
            for (int i = 1; i < 4; i++) {
                for (int j = 1; j < (int)(this.height / 4); j++) {
                    Point2D w1 = project(x - this.width/2 + i * 3, y + j * 4, z - depth/2 + 0.1, width, height);
                    Point2D w2 = project(x - this.width/2 + i * 3 + 2, y + j * 4 + 2, z - depth/2 + 0.1, width, height);
                    if (w1 != null && w2 != null) {
                        g2.setColor(new Color(200, 220, 255, 200));
                        g2.fillRect((int)w1.getX(), (int)w1.getY(), 
                                   (int)(w2.getX() - w1.getX()), (int)(w2.getY() - w1.getY()));
                    }
                }
            }
        }
        
        @Override
        void interact() {
            color = new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256));
            isPulsing = true;
            pulseTimer = 60;
        }
    }
    
    // Tree class
    class Tree extends WorldObject {
        Tree(double x, double y, double z) {
            super(x, y, z, new Color(139, 69, 19));
        }
        
        @Override
        void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted) {
            // Trunk
            Point2D b1 = project(x - 0.5, y, z - 0.5, width, height);
            Point2D b2 = project(x + 0.5, y, z - 0.5, width, height);
            Point2D t1 = project(x - 0.5, y + 8, z - 0.5, width, height);
            Point2D t2 = project(x + 0.5, y + 8, z - 0.5, width, height);
            
            if (b1 != null && b2 != null && t1 != null && t2 != null) {
                g2.setColor(color);
                int[] xPoints = {(int)b1.getX(), (int)b2.getX(), (int)t2.getX(), (int)t1.getX()};
                int[] yPoints = {(int)b1.getY(), (int)b2.getY(), (int)t2.getY(), (int)t1.getY()};
                g2.fillPolygon(xPoints, yPoints, 4);
            }
            
            // Foliage (layered circles)
            for (int i = 0; i < 3; i++) {
                Point2D top = project(x, y + 10 + i * 2, z, width, height);
                if (top != null) {
                    double dist = Math.sqrt(Math.pow(x - camX, 2) + Math.pow(z - camZ, 2));
                    int size = (int)(40 / Math.max(dist / 15, 1)) - i * 5;
                    g2.setColor(new Color(34, 139 - i * 20, 34));
                    g2.fillOval((int)top.getX() - size/2, (int)top.getY() - size/2, size, size);
                }
            }
        }
    }
    
    // Collectible Sphere class
    class CollectibleSphere extends WorldObject {
        double radius;
        double animOffset;
        double rotationAngle = 0;
        boolean collected = false;
        int glowIntensity = 0;
        
        CollectibleSphere(double x, double y, double z, double radius, Color color) {
            super(x, y, z, color);
            this.radius = radius;
            this.animOffset = Math.random() * Math.PI * 2;
        }
        
        @Override
        void update() {
            if (!collected) {
                y = 8 + Math.sin(System.currentTimeMillis() / 1000.0 + animOffset) * 2;
                rotationAngle += 0.02;
                glowIntensity = (int)(Math.sin(System.currentTimeMillis() / 200.0) * 50 + 50);
            }
        }
        
        @Override
        void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted) {
            if (collected) return;
            
            Point2D center = project(x, y, z, width, height);
            if (center != null) {
                double dist = Math.sqrt(Math.pow(x - camX, 2) + Math.pow(z - camZ, 2));
                int size = (int)(radius * 600 / Math.max(dist, 1));
                
                // Glow layers
                for (int i = 3; i > 0; i--) {
                    int glowSize = size + i * 15;
                    int alpha = Math.max(0, (glowIntensity - i * 30));
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                    g2.fillOval((int)center.getX() - glowSize/2, (int)center.getY() - glowSize/2, glowSize, glowSize);
                }
                
                // Main sphere
                Color drawColor = highlighted ? color.brighter() : color;
                g2.setColor(drawColor);
                g2.fillOval((int)center.getX() - size/2, (int)center.getY() - size/2, size, size);
                
                // Highlight
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillOval((int)center.getX() - size/3, (int)center.getY() - size/3, size/3, size/3);
                
                // Rotating ring
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
                g2.setStroke(new BasicStroke(3));
                int ringSize = size + 10;
                g2.drawArc((int)center.getX() - ringSize/2, (int)center.getY() - ringSize/2, 
                          ringSize, ringSize, (int)(rotationAngle * 180 / Math.PI), 120);
            }
        }
        
        @Override
        void interact() {
            if (!collected) {
                collected = true;
            }
        }
    }
    
    // Platform class
    class Platform extends WorldObject {
        double width, height, depth;
        
        Platform(double x, double y, double z, double w, double h, double d, Color color) {
            super(x, y, z, color);
            this.width = w;
            this.height = h;
            this.depth = d;
        }
        
        @Override
        void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted) {
            Color drawColor = highlighted ? color.brighter() : color;
            
            // Top face
            Point2D t1 = project(x - this.width/2, y + this.height, z - depth/2, width, height);
            Point2D t2 = project(x + this.width/2, y + this.height, z - depth/2, width, height);
            Point2D t3 = project(x + this.width/2, y + this.height, z + depth/2, width, height);
            Point2D t4 = project(x - this.width/2, y + this.height, z + depth/2, width, height);
            
            if (t1 != null && t2 != null && t3 != null && t4 != null) {
                int[] xPoints = {(int)t1.getX(), (int)t2.getX(), (int)t3.getX(), (int)t4.getX()};
                int[] yPoints = {(int)t1.getY(), (int)t2.getY(), (int)t3.getY(), (int)t4.getY()};
                g2.setColor(drawColor);
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.setColor(drawColor.darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(xPoints, yPoints, 4);
            }
            
            // Front face
            Point2D f1 = project(x - this.width/2, y, z - depth/2, width, height);
            Point2D f2 = project(x + this.width/2, y, z - depth/2, width, height);
            Point2D f3 = project(x + this.width/2, y + this.height, z - depth/2, width, height);
            Point2D f4 = project(x - this.width/2, y + this.height, z - depth/2, width, height);
            
            if (f1 != null && f2 != null && f3 != null && f4 != null) {
                int[] xPoints = {(int)f1.getX(), (int)f2.getX(), (int)f3.getX(), (int)f4.getX()};
                int[] yPoints = {(int)f1.getY(), (int)f2.getY(), (int)f3.getY(), (int)f4.getY()};
                g2.setColor(drawColor.darker());
                g2.fillPolygon(xPoints, yPoints, 4);
            }
        }
    }
    
    // Lamp Post class
    class LampPost extends WorldObject {
        double lightIntensity = 0;
        
        LampPost(double x, double y, double z) {
            super(x, y, z, new Color(80, 80, 80));
        }
        
        @Override
        void update() {
            lightIntensity = Math.sin(System.currentTimeMillis() / 500.0) * 0.3 + 0.7;
        }
        
        @Override
        void draw(Graphics2D g2, int width, int height, double camX, double camY, double camZ, double yaw, double pitch, boolean highlighted) {
            // Post
            Point2D b = project(x, y, z, width, height);
            Point2D t = project(x, y + 10, z, width, height);
            
            if (b != null && t != null) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(4));
                g2.drawLine((int)b.getX(), (int)b.getY(), (int)t.getX(), (int)t.getY());
            }
            
            // Light
            Point2D light = project(x, y + 10, z, width, height);
            if (light != null) {
                double dist = Math.sqrt(Math.pow(x - camX, 2) + Math.pow(z - camZ, 2));
                int size = (int)(20 / Math.max(dist / 20, 1));
                
                // Glow
                int alpha = (int)(lightIntensity * 150);
                g2.setColor(new Color(255, 220, 100, alpha));
                g2.fillOval((int)light.getX() - size, (int)light.getY() - size, size * 2, size * 2);
                
                // Bulb
                g2.setColor(new Color(255, 240, 150));
                g2.fillOval((int)light.getX() - size/2, (int)light.getY() - size/2, size, size);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Enhanced Virtual Reality Environment");
            VirtualRealityWorld env = new VirtualRealityWorld();
            frame.add(env);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Add ESC key handler to release mouse
            env.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "releaseMouse");
            env.getActionMap().put("releaseMouse", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    env.mouseCaptured = false;
                    env.setCursor(Cursor.getDefaultCursor());
                    env.showMessage("Mouse released");
                }
            });
        });
    }
}