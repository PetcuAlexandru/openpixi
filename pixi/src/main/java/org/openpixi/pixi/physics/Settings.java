package org.openpixi.pixi.physics;

import org.openpixi.pixi.diagnostics.methods.Diagnostics;
import org.openpixi.pixi.parallel.cellaccess.CellIterator;
import org.openpixi.pixi.parallel.cellaccess.ParallelCellIterator;
import org.openpixi.pixi.parallel.cellaccess.SequentialCellIterator;
import org.openpixi.pixi.parallel.particleaccess.ParallelParticleIterator;
import org.openpixi.pixi.parallel.particleaccess.ParticleIterator;
import org.openpixi.pixi.parallel.particleaccess.SequentialParticleIterator;
import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.grid.CloudInCell;
import org.openpixi.pixi.physics.grid.GridBoundaryType;
import org.openpixi.pixi.physics.grid.InterpolatorAlgorithm;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.particles.ParticleFactory.PositionDistribution;
import org.openpixi.pixi.physics.particles.ParticleFactory.VelocityDistribution;
import org.openpixi.pixi.physics.solver.Euler;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.util.ClassCopier;
import org.openpixi.pixi.physics.particles.ParticleFactory;
import org.openpixi.pixi.physics.particles.ParticleLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.openpixi.pixi.physics.GeneralBoundaryType.Hardwall;
import static org.openpixi.pixi.physics.GeneralBoundaryType.Periodic;

/**
 * Specifies default values of simulation parameters.
 * The default values can be overridden with a settings file input.
 * The default values can be overridden with a command line input.
 * The default values can be overridden programatically.
 *
 * NOTICE ON USAGE:
 * To assure that the class is used in the intended way
 * THE SETTERS SHOULD BE CALLED RIGHT AFTER A PARAMETERLESS CONSTRUCTOR
 * AND BEFORE ANY OF THE MORE COMPLEX GETTERS IS CALLED !!!
 *
 * In the tests of the distributed version we pass the same settings class to all the different
 * threads which simulate the distributed behaviour.
 * Consequently, the different simultaneously running simulations do have the same particle solver,
 * interpolator etc. and this is very dangerous.
 * TODO ensure that all the getters retrieve new objects
 */
public class Settings {

	//----------------------------------------------------------------------------------------------
	// DEFAULT VALUES
	//----------------------------------------------------------------------------------------------

	private double simulationWidth = 100;
	private double simulationHeight = 100;
	private double speedOfLight = 1;
	private double timeStep = 1;

	private GeneralBoundaryType boundaryType = GeneralBoundaryType.Periodic;

	private InterpolatorAlgorithm interpolator = new CloudInCell();

	// Grid related settings

	private int gridCellsX = 10;
	private int gridCellsY = 10;

	private FieldSolver gridSolver = new SimpleSolver();
	private PoissonSolver poissonSolver = new PoissonSolverFFTPeriodic();

	private boolean useGrid = true;

	// Particle related settings

	private int numOfParticles = 128;
	private double particleRadius = 1;
	private double particleMaxSpeed = speedOfLight/3;
	private int simulationType = 0;
        private int writeToFile = 0;
        private String OCLParticleSolver;
        private String OCLGridInterpolator;
        
	// Modify defaultParticleFactories() method to determine what kind of particles
	// will be loaded by default.
	private List<Particle> particles = new ArrayList<Particle>();

	private Detector collisionDetector = new Detector();
	private CollisionAlgorithm collisionResolver = new CollisionAlgorithm();
	private Solver particleSolver = new Euler();
	private List<Force> forces = new ArrayList<Force>();
	
	// Diagnostics related settings
	
	/** Used to mark output files */
	private String runid = "default-run";
	
	private List<Diagnostics> diagnostics = new ArrayList<Diagnostics>();
	
	// Batch version settings

	private int iterations = 1;

	// Parallel (threaded) version settings

	private int numOfThreads = 1;
	/* The creation and start of the new threads is expensive. Therefore, in the parallel
	 * simulation we use ExecutorService which is maintaining a fixed number of threads running
	 * all the time and assigns work to the threads on the fly according to demand. */
	private ExecutorService threadsExecutor;

	// Distributed version settings

	private int numOfNodes = 1;
	private String iplServer = "localhost";
	private String iplPool = "openpixi";

	//----------------------------------------------------------------------------------------------
	// SIMPLE GETTERS
	//----------------------------------------------------------------------------------------------
        public int getSimulationType(){
                return this.simulationType;
        }
        
        public int getWriteToFile(){
                return this.writeToFile;
        }
        
	public double getSimulationWidth() {
		return simulationWidth;
	}

	public double getSimulationHeight() {
		return simulationHeight;
	}

	public int getGridCellsX() {
		return gridCellsX;
	}

	public int getGridCellsY() {
		return gridCellsY;
	}

	public double getSpeedOfLight() {
		return speedOfLight;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public Detector getCollisionDetector() {
		return collisionDetector;
	}

	public CollisionAlgorithm getCollisionAlgorithm() {
		return collisionResolver;
	}

	public FieldSolver getGridSolver() {
		/*
		 * For the distributed tests to pass we need to create new grid solver so that the two
		 * simulation instances do not share the cell iterator!
		 */
		return gridSolver.clone();
	}

	public PoissonSolver getPoissonSolver() {
		return poissonSolver;
	}

	public Solver getParticleSolver() {
		return particleSolver;
	}

        public String getOCLParticleSolver(){
            return this.OCLParticleSolver;
        }
        
	public InterpolatorAlgorithm getInterpolator() {
		return interpolator;
	}
        
        public String getOCLGridInterpolator(){
            return this.OCLGridInterpolator;
        }
	
	public String getRunid() {
		return runid;
	}
	
	public List<Diagnostics> getDiagnostics() {
		return diagnostics;
	}

	public int getNumOfNodes() {
		return numOfNodes;
	}

	public GeneralBoundaryType getBoundaryType() {
		return boundaryType;
	}

	public int getIterations() {
		return iterations;
	}

	public String getIplServer() {
		return iplServer;
	}

	public boolean useGrid() {
		return useGrid;
	}

	public String getIplPool() {
		return iplPool;
	}

	//----------------------------------------------------------------------------------------------
	// MORE COMPLEX GETTERS / BUILDERS
	//----------------------------------------------------------------------------------------------

	public double getCellWidth() {
		return simulationWidth / gridCellsX;
	}

	public double getCellHeight() {
		return simulationHeight / gridCellsY;
	}

	/**
	 * Build the combined force for simulation.
	 */
	public CombinedForce getForce() {
		CombinedForce combinedForce = new CombinedForce();
		for (Force f: forces) {
			combinedForce.add(f);
		}
		return combinedForce;
	}

	private List<ParticleFactory> defaultParticleFactories() {
		// Random seed
		long seed = (long) Math.random();
		// If asList() is used the resulting list will have a fixed size!
		List<ParticleFactory> particleFactories = Arrays.asList(
				new ParticleFactory(numOfParticles/2, 1, 1, particleRadius, 
						PositionDistribution.RANDOM, VelocityDistribution.RANDOM, 
						particleMaxSpeed/10, particleMaxSpeed/10,particleMaxSpeed, 
						false, seed, seed),
				new ParticleFactory(numOfParticles/2, 1, -1, particleRadius, 
						PositionDistribution.RANDOM, VelocityDistribution.RANDOM, 
						particleMaxSpeed/10, particleMaxSpeed/10, particleMaxSpeed, 
						false, seed, seed));
		
		return particleFactories;
	}
	
	/**
	 * If no particles are specified creates random particles.
	 *
	 * !!! IMPORTANT !!!
	 * Always returns deep copy of the actual particle list!
	 */
	public List<Particle> getParticles() {
		if (particles.size() == 0) {
			this.particles = (new ParticleLoader()).load(defaultParticleFactories(), 
					simulationWidth, simulationHeight, gridCellsX, gridCellsY);
		}

		return cloneParticles();
	}


	private List<Particle> cloneParticles() {
		List<Particle> copy = new ArrayList<Particle>();
		for (Particle p: particles) {
			copy.add(p.copy());
		}
		return copy;
	}


	public GridBoundaryType getGridBoundary() {
		switch (boundaryType) {
			case Periodic:
				return GridBoundaryType.Periodic;
			case Hardwall:
				return GridBoundaryType.Hardwall;
			default:
				return GridBoundaryType.Hardwall;
		}
	}

	public ParticleBoundaryType getParticleBoundary() {
		switch (boundaryType) {
			case Periodic:
				return ParticleBoundaryType.Periodic;
			case Hardwall:
				return ParticleBoundaryType.Hardwall;
			default:
				return ParticleBoundaryType.Hardwall;
		}
	}

	public ParticleIterator getParticleIterator() {
		if (numOfThreads == 1) {
			return new SequentialParticleIterator();
		}
		else if (numOfThreads > 1) {
			return  new ParallelParticleIterator(numOfThreads, getThreadsExecutor());
		}
		else {
			throw new RuntimeException("Invalid number of threads: " + numOfThreads);
		}
	}

	public CellIterator getCellIterator() {
		if (numOfThreads == 1) {
			return new SequentialCellIterator();
		}
		else if (numOfThreads > 1) {
			return  new ParallelCellIterator(numOfThreads, getThreadsExecutor());
		}
		else {
			throw new RuntimeException("Invalid number of threads: " + numOfThreads);
		}
	}

	/**
	 * Create threads executor on the fly according to demand.
	 */
	private ExecutorService getThreadsExecutor() {
		if (threadsExecutor == null) {
			threadsExecutor = Executors.newFixedThreadPool(numOfThreads);
		}
		return threadsExecutor;
	}

	//----------------------------------------------------------------------------------------------
	// SETTERS (Overwrite default values programatically)
	//----------------------------------------------------------------------------------------------
        public void setSimulationType(int simulationType){
                this.simulationType = simulationType;
        }
        
        public void setWriteToFile(int writeTo){
                this.writeToFile = writeTo;
        }
        
	public void setSimulationWidth(double simulationWidth) {
		this.simulationWidth = simulationWidth;
	}

	public void setSimulationHeight(double simulationHeight) {
		this.simulationHeight = simulationHeight;
	}

	public void setGridCellsX(int gridCellsX) {
		this.gridCellsX = gridCellsX;
	}

	public void setGridCellsY(int gridCellsY) {
		this.gridCellsY = gridCellsY;
	}

	public void setSpeedOfLight(double speedOfLight) {
		this.speedOfLight = speedOfLight;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}

	public void setCollisionDetector(Detector collisionDetector) {
		this.collisionDetector = collisionDetector;
	}

	public void setCollisionResolver(CollisionAlgorithm collisionResolver) {
		this.collisionResolver = collisionResolver;
	}

	public void setGridSolver(FieldSolver gridSolver) {
		this.gridSolver = gridSolver;
	}

	public void setPoissonSolver(PoissonSolver poissonSolver) {
		this.poissonSolver = poissonSolver;
	}

	public void setParticleSolver(Solver particleSolver) {
		this.particleSolver = particleSolver;
	}

        public void setOCLParticleSolver(String OCLParticleSolver){
                this.OCLParticleSolver = OCLParticleSolver;
        }
        
	public void setInterpolator(InterpolatorAlgorithm interpolator) {
		this.interpolator = interpolator;
	}
	
        public void setOCLGridInterpolator(String OCLGridInterpolator){
                this.OCLGridInterpolator = OCLGridInterpolator;
        }
        
	public void setRunid (String runid) {
		this.runid = runid;
	}
	
	public void  setDiagnostics(List<Diagnostics> diagnostics) {
		this.diagnostics = diagnostics;
	}

	public void setForces(List<Force> forces) {
		this.forces = forces;
	}

	public void addForce(Force force) {
		this.forces.add(force);
	}

	public void setNumOfParticles(int numOfParticles) {
		this.numOfParticles = numOfParticles;
	}

	public void setParticleList(List<Particle> particles) {
		this.numOfParticles = particles.size();
		this.particles = particles;
	}
	
	public void setParticleRadius(double particleRadius) {
		this.particleRadius = particleRadius;
	}

	public void setParticleMaxSpeed(double particleMaxSpeed) {
		this.particleMaxSpeed = particleMaxSpeed;
	}

	public void addParticle(Particle p) {
		particles.add(p);
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}

	public void setBoundary(GeneralBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
	}

	public void setIplServer(String iplServer) {
		this.iplServer = iplServer;
	}

	public void useGrid(boolean useGrid) {
		this.useGrid = useGrid;
	}

	public void setIplPool(String iplPool) {
		this.iplPool = iplPool;
	}

	public void setNumOfThreads(int numOfThreads) {
		this.numOfThreads = numOfThreads;
	}

	//----------------------------------------------------------------------------------------------
	// VARIOUS
	//----------------------------------------------------------------------------------------------
	
	public Settings() {
	}

	/**
	 * Overwrites default values with file input.
	 */
	public Settings(String fileName) {
		this();
		// Parse file
		throw new UnsupportedOperationException();
	}

	/**
	 * Overwrites default values with command line input.
	 */
	public Settings(String[] cmdLine) {
		throw new UnsupportedOperationException();
	}


	/**
	 * Has to be called every time numOfThreads is set to a value higher than 1!
	 * Terminates the threads used by executor service.
	 * Is idempotent (can be called multiple times without side-effects).
	 */
	public void terminateThreads() {
		if (threadsExecutor != null) {
			threadsExecutor.shutdown();
			threadsExecutor = null;
		}
	}
}
