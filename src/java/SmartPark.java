// Environment code for project smart_park

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.*;

public class SmartPark extends Environment {

    private Logger logger = Logger.getLogger("smart_park."+SmartPark.class.getName());
    public static final int GSize = 7; // grid size
    public static final int GRASS  = 16;
    public static final int WET  = 8;
    
    public static final Literal g = Literal.parseLiteral("grass");
    
    public static final Term f = Literal.parseLiteral("findgrass");
    private ParkModel model;
    private ParkView  view;
    int dryCounter=1;
    int growCounter=1;
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        model = new ParkModel();
        view  = new ParkView(model);
        model.setView(view);
        addPercept(Literal.parseLiteral("biggrassexists"));
        addPercept(Literal.parseLiteral("noteverythingwet"));
        updatePercepts();
    }
    
    void updatePercepts() {
        Location lm1Loc = model.getAgPos(0);
        Location lm2Loc = model.getAgPos(1);
        if (model.hasObject(WET, lm1Loc)) {
            Literal wet = Literal.parseLiteral("pos(wet," + lm1Loc.x + "," + lm1Loc.y + ")");
            addPercept("lm1", wet);
        } else {
        	Literal wet = Literal.parseLiteral("pos(wet," + lm1Loc.x + "," + lm1Loc.y + ")");
            removePercept("lm1", wet);
        }
        if (model.hasObject(WET, lm2Loc)) {
            Literal wet = Literal.parseLiteral("pos(wet," + lm2Loc.x + "," + lm2Loc.y + ")");
            addPercept("lm2", wet);
        }else {
        	Literal wet = Literal.parseLiteral("pos(wet," + lm2Loc.x + "," + lm2Loc.y + ")");
            removePercept("lm2", wet);
        }
        if (model.hasObject(GRASS, lm1Loc)) {
        	addPercept("lm1",g);
        }
        if (model.hasObject(GRASS, lm2Loc)) {
        	addPercept("lm2",g);
        }
        if(model.countObjects(GRASS)==0) {
        	removePercept(Literal.parseLiteral("biggrassexists"));
        } else {
        	addPercept(Literal.parseLiteral("biggrassexists"));
        }
        if(model.countObjects(GRASS)==36) {
        	removePercept(Literal.parseLiteral("noteverythingwet"));
        } else {
        	addPercept(Literal.parseLiteral("noteverythingwet"));
        }
        maybeDryTile();
        maybeGrowAWetGrass();
        view.repaint();
    }
    
    void maybeDryTile() {
    	if(dryCounter%5==0) {
        	ArrayList<Location> wets = new ArrayList<Location>();
        	for(int x=0; x<GSize; x++) {
        		for(int y=0; y<GSize; y++) {
        			if(model.hasObject(WET, x, y));
        				wets.add(new Location(x, y));
        		}
        	}
        	Location loc = wets.get(new Random().nextInt(wets.size()));
        	model.remove(WET, loc);
        	dryCounter=1;
    	}else
    		dryCounter++;
    }

    void maybeGrowAWetGrass() {
    	if(growCounter%15==0) {
        	ArrayList<Location> wets = new ArrayList<Location>();
        	for(int x=0; x<GSize; x++) {
        		for(int y=0; y<GSize; y++) {
        			if(model.hasObject(WET, x, y) && !model.hasObject(GRASS, x, y));
        				wets.add(new Location(x, y));
        		}
        	}
        	Location loc = wets.get(new Random().nextInt(wets.size()));
        	model.remove(WET, loc);
        	model.add(GRASS, loc);
        	growCounter=1;
    	}else
    		growCounter++;
    }
    
    @Override
    public boolean executeAction(String agName, Structure action){
        logger.info("agent: "+ agName +" executing: "+action+"");
        try {
        	if (action.getFunctor().equals("mow")) {
        		int x = (int)((NumberTerm)action.getTerm(0)).solve();
		        int y = (int)((NumberTerm)action.getTerm(1)).solve();
	    		model.mowGrass(new Location(x, y));
	    		removePercept(agName,g);
	    	}else if (action.getFunctor().equals("addgrass")) {
        		int x = (int)((NumberTerm)action.getTerm(0)).solve();
		        int y = (int)((NumberTerm)action.getTerm(1)).solve();
	    		model.add(GRASS,new Location(x, y));
	    	}else if (action.getFunctor().equals("nextdest")) {
        		int x = (int)((NumberTerm)action.getTerm(0)).solve();
		        int y = (int)((NumberTerm)action.getTerm(1)).solve();
		        Location destLoc = model.planDest(new Location(x, y));
		        addPercept(agName, Literal.parseLiteral("pos(next," + destLoc.x + "," + destLoc.y + ","+agName+")"));
	    	} else if (action.getFunctor().equals("move")) {
		            int x = (int)((NumberTerm)action.getTerm(0)).solve();
		            int y = (int)((NumberTerm)action.getTerm(1)).solve();
		            String agent  = action.getTerm(2).toString();
		            int i = Integer.parseInt( agent.substring(2) );
		            model.move(x,y,i-1);
		            removePercept(agName,Literal.parseLiteral("pos(next," + x + "," + y +","+agName+ ")"));
	    	} else if (action.getFunctor().equals("dontMove")) {
	            int x = (int)((NumberTerm)action.getTerm(0)).solve();
	            int y = (int)((NumberTerm)action.getTerm(1)).solve();
	            String agent  = action.getTerm(2).toString();
	            removePercept(agName,Literal.parseLiteral("pos(next," + x + "," + y +","+agName+ ")"));
	    	}else if (action.getFunctor().equals("moveVis")) {
	            int x = (int)((NumberTerm)action.getTerm(0)).solve();
	            int y = (int)((NumberTerm)action.getTerm(1)).solve();
	            model.move(x,y,3);
	            removePercept(agName,Literal.parseLiteral("pos(next," + x + "," + y +","+agName+ ")"));
	            
	    	}else if (action.equals(f)) {
	    		Location l = model.findgrass();
	    		addPercept(agName, Literal.parseLiteral("sprinkle(" + l.x + "," + l.y+")"));
	    	} else if (action.getFunctor().equals("sprinkle")) {
	            int x = (int)((NumberTerm)action.getTerm(0)).solve();
	            int y = (int)((NumberTerm)action.getTerm(1)).solve();
	            model.sprinkle(new Location(x,y));	       
	            removePercept(agName, Literal.parseLiteral("sprinkle(" + x + "," + y+")"));
	    	} else if (action.getFunctor().equals("randNext")) {
	            int x = (int)((NumberTerm)action.getTerm(0)).solve();
	            int y = (int)((NumberTerm)action.getTerm(1)).solve();
	            Location destLoc = model.randDest(new Location(x, y));
	            addPercept(agName, Literal.parseLiteral("pos(next," + destLoc.x + "," + destLoc.y + ","+agName+")"));
	    	}
	    	else {
	    		System.out.println("no command found");
	    		return false;
	    	}
        }catch (Exception e) {
			System.out.println("exeptio in command execution");
        }
        updatePercepts();
        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    class ParkModel extends GridWorldModel {

        Random random = new Random(System.currentTimeMillis());

        private ParkModel() {
            super(GSize, GSize, 4);
            try {
                setAgPos(0, 3, 3);
                setAgPos(1, 3, 3);
                setAgPos(2, 6, 6);
                setAgPos(3, 1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            add(GRASS, 4, 4);
            add(GRASS, 5, 2);
            add(GRASS, 5, 4);
            add(GRASS, 2, 2);
            add(GRASS, 4, 4);
            
        }
        
        public void sprinkle(Location loc) {
        	model.add(WET, loc);
		}

		public Location findgrass() {
        	ArrayList<Location> grass = new ArrayList<Location>();
        	for(int x=0; x<GSize; x++) {
        		for(int y=0; y<GSize; y++) {
        			if(!model.hasObject(AGENT, new Location(x, y))) {
        				Location loc = new Location(x,y);
        				grass.add(loc);
        			}
        		}
        	}
        	
			return grass.get(new Random().nextInt(grass.size()));
		}

		void mowGrass(Location loc) {
        	model.remove(GRASS, loc);
        }
        
        void move(int x,int y,int agent) {
        	setAgPos(agent, x, y);
        }
        
    
        Location randDest(Location loc) {
        	int x = loc.x;
        	int y = loc.y;
        	ArrayList<Location> neigbors = new ArrayList<Location>();
        	neigbors.add(new Location(x-1, y));
        	neigbors.add(new Location(x+1, y));
        	neigbors.add(new Location(x, y-1));
        	neigbors.add(new Location(x, y+1));
        	ArrayList<Location> avalaibleNeigbors = new ArrayList<Location>();
        	for(Location l : neigbors) {
        		if(!hasObject(AGENT, l) && inGrid(l)) {
        			avalaibleNeigbors.add(l);
        		}
        	}
    		Random rand = new Random();
    		int i = rand.nextInt(avalaibleNeigbors.size());
    		return avalaibleNeigbors.get(i);
        	
        }
        Location planDest(Location loc) {
        	int x = loc.x;
        	int y = loc.y;
        	ArrayList<Location> neigbors = new ArrayList<Location>();
        	neigbors.add(new Location(x-1, y));
        	neigbors.add(new Location(x+1, y));
        	neigbors.add(new Location(x, y-1));
        	neigbors.add(new Location(x, y+1));
        	ArrayList<Location> avalaibleNeigbors = new ArrayList<Location>();
        	for(Location l : neigbors) {
        		if(!hasObject(AGENT, l) && inGrid(l)) {
        			avalaibleNeigbors.add(l);
        		}
        	}
    		if(avalaibleNeigbors.isEmpty())
    			return loc;
        	
        	ArrayList<Location> avalaibleNeigborsWithGrass = new ArrayList<Location>();
        	for(Location l : avalaibleNeigbors) {
        		if(hasObject(GRASS, l) && inGrid(l) && !hasObject(WET, l)) {
        			avalaibleNeigborsWithGrass.add(l);
        		}
        	}
        	
        	if(avalaibleNeigborsWithGrass.isEmpty()) {
        		Random rand = new Random();
        		int i = rand.nextInt(avalaibleNeigbors.size());
        		return avalaibleNeigbors.get(i);
        	} else {
        		Random rand = new Random();
        		int i = rand.nextInt(avalaibleNeigborsWithGrass.size());
        		return avalaibleNeigborsWithGrass.get(i);
        	}
        }
    }

    class ParkView extends GridWorldView {

        public ParkView(ParkModel model) {
            super(model, "The Park", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            if( SmartPark.GRASS == object) {
            	g.setColor(Color.GREEN);
            	drawString(g,x,y,defaultFont,"Nagy fu");
            }
            if(SmartPark.GRASS + SmartPark.WET == object) {
            	g.setColor(Color.CYAN);
            	drawString(g,x,y,defaultFont,"Nagy vizes fu");
            }
            if( SmartPark.WET == object) {
            	g.setColor(Color.BLUE);
            	drawString(g,x,y,defaultFont,"Vizes");
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	String label = "";
        	if(id == 0 || id == 1)
        		label = "LM"+(id+1);
        	if(id == 2)
        		label = "SPR"+(id+1);
        	if(id == 3)
        		label = "VIS"+(id+1);
            c = Color.blue;
            super.drawAgent(g, x, y, c, -1);
            g.setColor(Color.white);
            super.drawString(g, x, y, defaultFont, label);
        }

    }
}
