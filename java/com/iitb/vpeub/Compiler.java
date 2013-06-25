package com.iitb.vpeub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



import com.iitb.vpeconfig.Config;
import com.iitb.vpeconfig.Target;

/**
 * Class that takes care of compiling pure c/cpp code to a hex,
 * to be uploaded to the board
 */
public class Compiler {

	//Directory where code is placed (ino)
	File codeDir;

	//Directory where built files are placed
	String buildDir;

	//Absolute path to the code
	String codePath;

	//File name of the cpp file to be generated
	String fileName;

	//Folder for all arduino headers
	File libFolder;

	//path to the core folder corresponding to the board
	String corePath;

	//path to the variant corresponding to the board
	String variantPath;

	String avrBasePath;
	PdePreprocessor preprocessor;

	// Absolute path of included header files
	ArrayList<File> importedLibraries;

	//where to build the code
	String buildPath;

	// Maps each Header file to its absolute path
	static HashMap<String, File> importToLibraryTable;
	
	//Name of archived core file
	String coreFileName;

	//The target board
	Target target;
	
	//Namr of generated elf
	String elfName;
	
	//stream for displaying erros
	PrintStream out;
	
	//Stream for errors
	PrintStream error;


	//base directory for all operations
	String base;
	String TAG = "Compiler Class";


	public Compiler(String board,String base,OutputStream out,OutputStream error) {
		this.base = base;
		this.out = new PrintStream(out);
		this.error = new PrintStream(error);
		target = null;
		if(board.equals("uno")) {
			target = Config.uno;
		}
		if(board.equals("atmega328")) {
			target = Config.atmega328;
		}
		
		if(target == null) {
			this.error.println("Board not Found");
			return;
		}
		
		File baseFolder = new File(base);
		fileName = "sketch.ino";
		codeDir = new File(baseFolder,"code");
		libFolder = new File(base,"libraries");
		avrBasePath = base + File.separator + "tools/avr/bin";
		corePath = base + File.separator + "cores"+File.separator+target.getCore();
		variantPath = base + File.separator +  "variants"+File.separator+target.getVariant();
		buildPath = base + File.separator + "build";
		coreFileName = "core.a";
		elfName = "final";
		


		// Linux specific command to remove clean build folder

		/*try {
			org.apache.commons.io.FileUtils.cleanDirectory(new File(buildPath));
			out.println("Build folder cleaned");
		} catch (IOException e) {
			out.println("I cant clean the nuild folder");
		}*/

	}

	/**
	 * Preprocess sketch.ino and output the cpp file in build folder
	 * @throws Exception
	 */
	public  void preProcess() throws Exception  {

		out.println("Preprocessing Started");
		preprocessor = new PdePreprocessor(base);

		String code = readFile(new File(codeDir,fileName));


		preprocessor.writePrefix(code , fileName, null);
		preprocessor.write();

		//out.println("pre Ex imports" + preprocessor.getExtraImports());


		//All files/folders in libFolder
		String list[] = libFolder.list(null);

		for(String l:list) {
			//out.println("lib file = " +l);
		}

		importToLibraryTable = new HashMap<String,File>();

		//Map all .h files to 
		for (String libraryName : list) {
			File subfolder = new File(libFolder, libraryName);

			//libraries.add(subfolder);
			try {
				String packages[] =
						headerListFromIncludePath(subfolder.getAbsolutePath());
				for (String pkg : packages) {
					importToLibraryTable.put(pkg, subfolder);
				}
			} catch (IOException e) {
				error.println(e.getMessage());
			}


		}


		//out.println("Header Table" + importToLibraryTable.toString());
		out.println("Preprocessing Ended");


		out.println("Listing imports");

		importedLibraries = new ArrayList<File>();

		for (String item : preprocessor.getExtraImports()) {
			File libPath = (File) importToLibraryTable.get(item);

			if (libPath != null && !importedLibraries.contains(libPath)) {
				importedLibraries.add(libPath);
				//out.println(libPath);
				//libraryPath += File.pathSeparator + libFolder.getAbsolutePath();
			}
		}

		out.println("Listing imports...done");


	}

	/***
	 * Do everything necessary for compilation
	 * Many steps are needed to get the final hex file,
	 * Comments inside the function will make it clear
	 */
	public void compile() {


		out.println("Compilation started");

		//List of all the include paths for all the .h files
		List<String> includePaths = new ArrayList<String>();

		//This path contains Ardhuino.h for the core, along with other files
		includePaths.add(corePath);


		//This path contains header files containing pin mappings
		if (variantPath != null) includePaths.add(variantPath);
		for (File file : importedLibraries) {
			includePaths.add(file.getPath());
		}

		//out.println("INC paths = " + importedLibraries.toString());


		List<File> objectFiles = new ArrayList<File>();

		out.println("Step 1. - Compiling user's source files");



		/*
		 * ************************** STEP 1 ************************************
		 */
		// 1. Compile users source files, in this case it will compile only one file , 
		// the file generated through Blockly
		objectFiles.addAll(
				compileFiles(avrBasePath, buildPath, includePaths,
						findFilesInPath(buildPath, "S", false),
						findFilesInPath(buildPath, "c", false),
						findFilesInPath(buildPath, "cpp", false),
						target));
		//sketchIsCompiled = true;

		out.println("Step 2. - Compiling included libraries");





		/*
		 * ************************* STEP 2 ************************************
		 */
		// 2. compile the libraries, outputting .o files to: <buildPath>/<library>/

		for (File libraryFolder : importedLibraries) {
			File outputFolder = new File(buildPath, libraryFolder.getName());
			File utilityFolder = new File(libraryFolder, "utility");
			createFolder(outputFolder);
			// this library can use includes in its utility/ folder
			includePaths.add(utilityFolder.getAbsolutePath());
			objectFiles.addAll(
					compileFiles(avrBasePath, outputFolder.getAbsolutePath(), includePaths,
							findFilesInFolder(libraryFolder, "S", false),
							findFilesInFolder(libraryFolder, "c", false),
							findFilesInFolder(libraryFolder, "cpp", false),
							target));
			outputFolder = new File(outputFolder, "utility");
			createFolder(outputFolder);
			objectFiles.addAll(
					compileFiles(avrBasePath, outputFolder.getAbsolutePath(), includePaths,
							findFilesInFolder(utilityFolder, "S", false),
							findFilesInFolder(utilityFolder, "c", false),
							findFilesInFolder(utilityFolder, "cpp", false),
							target));
			// other libraries should not see this library's utility/ folder
			includePaths.remove(includePaths.size() - 1);
		}

		out.println("Step 3 -  Compiling cores files");




		/*
		 * ************************* STEP 3 ************************************
		 */
		// 3. Compile files for the core, and combine them all in core.a

		includePaths.clear();
		includePaths.add(corePath);  // include path for core only
		if (variantPath != null) includePaths.add(variantPath);
		List<File> coreObjectFiles =
				compileFiles(avrBasePath, buildPath, includePaths,
						findFilesInPath(corePath, "S", true),
						findFilesInPath(corePath, "c", true),
						findFilesInPath(corePath, "cpp", true),
						target);

		String runtimeLibraryName = buildPath + File.separator + coreFileName;
		List<String> baseCommandAR = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath +File.separator + "avr-ar",
				"rcs",
				runtimeLibraryName
		}));



		out.println("Step 4 - Archiving core files");



	
		//out.println("Core files = " + coreObjectFiles.size());
		for(File file : coreObjectFiles) {
			List<String> commandAR = new ArrayList<String>(baseCommandAR);
			commandAR.add(file.getAbsolutePath());
			execute(commandAR);
		} 

		out.println("Step 5 - Generating ELF");



		/*
		 * ********************** STEP 4 *****************************
		 */
		// 4. link it all together into the .elf file
		// For atmega2560, need --relax linker option to link larger
		// programs correctly.
		String optRelax = "";
		String atmega2560 = new String ("atmega2560");
		if ( atmega2560.equals(target.getMCU()) ) {
			optRelax = new String(",--relax");
		}



		List<String> baseCommandLinker = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath + File.separator + "avr-gcc",
				"-Os",
				"-Wl,--gc-sections"+optRelax,
				"-mmcu=" + target.getMCU(),
				//"-save-temps=obj",
				"-o",
				buildPath + File.separator + elfName + ".elf",


		}));

		for (File file : objectFiles) {
			baseCommandLinker.add(file.getAbsolutePath());
		}

		baseCommandLinker.add(runtimeLibraryName);
		baseCommandLinker.add("-L" + buildPath);
		baseCommandLinker.add("-lm");
		//Log.wtf(TAG, "linker = " + baseCommandLinker.toString());
		//baseCommandLinker.add("-save-temps=obj");

		execute(baseCommandLinker);


		out.println("Step 6 - Doing EEPROM stuff");





		/*
		 * ********************** STEP 5 *****************************
		 */
		// 5. I am not sure

		List<String> baseCommandObjcopy = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath +File.separator+ "avr-objcopy",
				"-O",
				"-R",
		}));

		List<String> commandObjcopy;

		commandObjcopy = new ArrayList<String>(baseCommandObjcopy);
		commandObjcopy.add(2, "ihex");
		commandObjcopy.set(3, "-j");
		commandObjcopy.add(".eeprom");
		commandObjcopy.add("--set-section-flags=.eeprom=alloc,load");
		commandObjcopy.add("--no-change-warnings");
		commandObjcopy.add("--change-section-lma");
		commandObjcopy.add(".eeprom=0");
		commandObjcopy.add(buildPath + File.separator + elfName + ".elf");
		commandObjcopy.add(buildPath + File.separator + elfName + ".eep");
		execute(commandObjcopy);


		out.println("Step 7 - Generating hex");



		/*
		 * ********************** STEP 6 *****************************
		 */
		// 6. Generating hex
		commandObjcopy = new ArrayList<String>(baseCommandObjcopy);
		commandObjcopy.add(2, "ihex");
		commandObjcopy.add(".eeprom"); // remove eeprom data
		commandObjcopy.add(buildPath + File.separator + elfName + ".elf");
		commandObjcopy.add(buildPath + File.separator + elfName + ".hex");
		//Log.wtf(TAG, commandObjcopy.toString());
		execute(commandObjcopy);


		out.println("Compilation ended");





	}


	/**
	 * Read a file and output as a string
	 * @param f - the File
	 * @return - the String
	 * @throws IOException
	 */
	private String readFile(File f) throws IOException {
		InputStream in = new FileInputStream(f);
		byte[] b  = new byte[in.available()];
		int len = b.length;
		int total = 0;

		while (total < len) {
			int result = in.read(b, total, len - total);
			if (result == -1) {
				break;
			}
			total += result;
		}
		in.close();
		return new String( b );

	}

	/**
	 * Returns list of strings of files in path with ".h" extension
	 * @param path - the path to be searched
	 * @return - array of atrings
	 * @throws IOException
	 */
	static public String[] headerListFromIncludePath(String path) throws IOException {
		FilenameFilter onlyHFiles = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".h");
			}
		};

		String[] list = (new File(path)).list(onlyHFiles);
		if (list == null) {
			throw new IOException();
		}
		return list;
	}

	/**
	 * Returns List of files in path with required extension
	 * @param path - the path to search
	 * @param extension - extension to be searched
	 * @param recurse - whether or not to recurse
	 * @return - ArrayList of Files
	 */
	static public ArrayList<File> findFilesInPath(String path, String extension,
			boolean recurse) {
		return findFilesInFolder(new File(path), extension, recurse);
	}
	/**
	 * 
	 * @param folder -  file pointing to folder to search for
	 * @param extension - extension to search for
	 * @param recurse - whether or not to recurse
	 * @return - ArrayList of Files
	 */
	static public ArrayList<File> findFilesInFolder(File folder, String extension,
			boolean recurse) {
		ArrayList<File> files = new ArrayList<File>();

		if (folder.listFiles() == null) return files;

		for (File file : folder.listFiles()) {
			if (file.getName().startsWith(".")) continue; // skip hidden files

			if (file.getName().endsWith("." + extension))
				files.add(file);

			if (recurse && file.isDirectory()) {
				files.addAll(findFilesInFolder(file, extension, true));
			}
		}

		return files;
	}

	/**
	 * Invokes the compiler to compile the c,s and cpp files into the builpath
	 * @param avrBasePath - path where compiler resides
	 * @param buildPath - path where to output the compiled files
	 * @param includePaths - path for all headers
	 * @param sSources - C Files
	 * @param cSources - S Files
	 * @param cppSources -  cpp files
	 * @param target - the board to compile for
	 * @return - List of File of the object files formed
	 */
	private List<File> compileFiles(String avrBasePath,
			String buildPath, List<String> includePaths,
			List<File> sSources, 
			List<File> cSources, List<File> cppSources,
			Target target)
			{

		List<File> objectPaths = new ArrayList<File>();



		for (File file : sSources) {
			String objectPath = buildPath + File.separator + file.getName() + ".o";
			objectPaths.add(new File(objectPath));
			List<String> comm = getCommandCompilerS(avrBasePath, includePaths,
					file.getAbsolutePath(),
					objectPath,
					target);

			//out.println(comm.toString());
			execute(comm);

		}

		for (File file : cSources) {
			String objectPath = buildPath + File.separator + file.getName() + ".o";
			File objectFile = new File(objectPath);
			objectPaths.add(objectFile);

			List<String> comm = getCommandCompilerC(avrBasePath, includePaths,
					file.getAbsolutePath(),
					objectPath,
					target);

			//out.println(comm.toString());
			execute(comm);
		}

		for (File file : cppSources) {
			String objectPath = buildPath + File.separator + file.getName() + ".o";
			File objectFile = new File(objectPath);
			objectPaths.add(objectFile);
			List<String> comm = getCommandCompilerCPP(avrBasePath, includePaths,
					file.getAbsolutePath(),
					objectPath,
					target);

			//out.println(comm.toString());
			execute(comm);
		}

		return objectPaths;
			}

	/**
	 * Take the parameters and generate a command to invoke gcc , g++ etc.
	 * Generates command for compiling S files
	 * @param avrBasePath - path to gcc, etc.
	 * @param includePaths - paths to include for header files
	 * @param sourceName - name of .S file
	 * @param objectName - name of .o file
	 * @param t - the target object
	 * @return
	 */
	private List<String> getCommandCompilerS(String avrBasePath, List<String> includePaths,
			String sourceName, String objectName, Target t) {
		List<String> baseCommandCompiler = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath +File.separator + "avr-gcc",
				"-c", // compile, don't link
				"-g", // include debugging info (so errors include line numbers)
				"-assembler-with-cpp",
				"-mmcu=" + t.getMCU(),
				"-DF_CPU=" + t.getFCPU(),      
				"-DARDUINO=" + Config.REVISION,
				"-DUSB_VID=" + t.getVID(),
				"-DUSB_PID=" + t.getPID(),
				//"-save-temps=obj",//To create temporary files in same dir as o/p files
		}));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompiler.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompiler.add(sourceName);
		baseCommandCompiler.add("-o"+ objectName);

		return baseCommandCompiler;
	}

	/**
	 * Take the parameters and generate a command to invoke gcc , g++ etc.
	 * Generates command for compiling C files
	 * @param avrBasePath - path to gcc, etc.
	 * @param includePaths - paths to include for header files
	 * @param sourceName - name of .S file
	 * @param objectName - name of .o file
	 * @param t - the target object
	 * @return
	 */
	static private List<String> getCommandCompilerC(String avrBasePath, List<String> includePaths,
			String sourceName, String objectName, Target t) {

		List<String> baseCommandCompiler = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath +File.separator+ "avr-gcc",
				"-c", // compile, don't link
				"-g", // include debugging info (so errors include line numbers)
				"-Os", // optimize for size
				Config.verbose ? "-Wall" : "-w", // show warnings if verbose
						"-ffunction-sections", // place each function in its own section
						"-fdata-sections",
						"-mmcu=" + t.getMCU(),
						"-DF_CPU=" + t.getFCPU(),
						"-MMD", // output dependancy info
						"-DUSB_VID=" + t.getVID(),
						"-DUSB_PID=" + t.getPID(),
						"-DARDUINO=" + Config.REVISION, 
						//"-save-temps=obj",//To create temporary files in same dir as o/p files
		}));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompiler.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompiler.add(sourceName);
		baseCommandCompiler.add("-o");
		baseCommandCompiler.add(objectName);

		return baseCommandCompiler;
	}



	/**
	 * Take the parameters and generate a command to invoke gcc , g++ etc.
	 * Generates command for compiling CPP files
	 * @param avrBasePath - path to gcc, etc.
	 * @param includePaths - paths to include for header files
	 * @param sourceName - name of .S file
	 * @param objectName - name of .o file
	 * @param t - the target object
	 * @return
	 */
	static private List<String> getCommandCompilerCPP(String avrBasePath,
			List<String> includePaths, String sourceName, String objectName,
			Target t) {

		List<String> baseCommandCompilerCPP = new ArrayList<String>(Arrays.asList(new String[] {
				avrBasePath +File.separator+ "avr-g++",
				"-c", // compile, don't link
				"-g", // include debugging info (so errors include line numbers)
				"-Os", // optimize for size
				Config.verbose  ? "-Wall" : "-w", // show warnings if verbose
						"-fno-exceptions",
						"-ffunction-sections", // place each function in its own section
						"-fdata-sections",
						"-mmcu=" + t.getMCU(),
						"-DF_CPU=" + t.getFCPU(),
						"-MMD", // output dependancy info
						"-DUSB_VID=" + t.getVID(),
						"-DUSB_PID=" + t.getPID(),      
						"-DARDUINO=" + Config.REVISION,
						//"-save-temps=obj",//To create temporary files in same dir as o/p files
		}));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompilerCPP.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompilerCPP.add(sourceName);
		baseCommandCompilerCPP.add("-o");
		baseCommandCompilerCPP.add(objectName);

		return baseCommandCompilerCPP;
	}

	/**
	 * Execute the command specified
	 * Compiler may generate error messages, they will be spewed out on the LogCat
	 * @param commandList - List of commands "avr-gcc,-c,-whatever etc etc "
	 */
	private void execute(List<String> commandList) {
		String[] command = new String[commandList.size()];
		commandList.toArray(command);
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(commandList);
			//pb.directory(new File(buildPath));
			process = pb.start();//Runtime.getRuntime().exec(command);
			InputStream is = process.getErrorStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			while(line!=null)
			{
				String comm = (String)commandList.get(0);
				String pname = comm.substring(comm.lastIndexOf(File.separator));
				out.println(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			error.println(e.getMessage());
			out.println("Failure to exec");
		}



	}
	private void createFolder(File folder)  {
		if (folder.isDirectory()) return;
		if (!folder.mkdir())
			out.println("I can't create the folder: " + folder.getAbsolutePath() );
	}
	
	public void doYourJob() {
		try {
			preProcess();
			compile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			error.println(e.getMessage());
		}
	}
}


