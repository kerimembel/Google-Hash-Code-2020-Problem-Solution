import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main {
									//Needed Variables
	static int book_count= 0;
	static int library_count = 0;
	static int day_count = 0;
	
	static ArrayList<Book> books = new ArrayList<Book>();
	static ArrayList<Library> libraries = new ArrayList<Library>();
	
	static String[] filenames = { "a_example", "b_read_on", "c_incunabula", "d_tough_choices", "e_so_many_books", "f_libraries_of_the_world" } ;
	//static long[] bests = {21,5822900,5640234,4841980,4997725,5292375};
	static long[] bests = {0,0,0,0,0,0}; //Keeping Best Scores 
	
	
	public static void main(String[] args) throws IOException {
		ReadFiles();	
	}	
										//Read files and simulate one by one 
	static void ReadFiles() throws IOException {
		
		for (int i = 0; i < filenames.length; i++) {
			
			ReadDoc(filenames[i]);
			Simulate(filenames[i]);
			
		}
	}
	
		
	static void Simulate(String filename) {
		
		long score = 0;				//Calculated score
		int signed_libraries = 0;	//Signed Library count
		int[] books_scanned_already = new int[books.size()];	//Already scanned books
		ScanObject temp = new ScanObject();						//An object to keep multiple variables.

		Collections.sort(libraries, WeightComparator);			//Sort Libraries by their weights

		ArrayList<OrderObject> library_order = new ArrayList<OrderObject>();	//Arraylist to keep which library signed 																
																				//and which books are scanneed
		
		//Start looking through the sorted library list in order and continue until there is time
		for (Library lib : libraries) {
			day_count = day_count - lib.sign_time;
			if(day_count >= 0) {
				temp = ScanBooks(lib, day_count, books_scanned_already);
				
				if(temp.score > 0) {
					score += temp.score;
					signed_libraries++;
					library_order.add(new OrderObject(lib.id, temp.books_scanned_already));
					books_scanned_already = temp.prev_scanned_books;
				}
				
				else {
					day_count += lib.sign_time;
				}
			}
		}
		
		//Find previous best score 
		long best_score = 0;
		for (int i = 0; i < filenames.length; i++) {
			if(filename == filenames[i])
				best_score = bests[i];
		}
		
		System.out.println("Score    " + score + " for "+ filename);
		System.out.println("Best was " + best_score + "\n");
	
		//If current score is better than best score than write into file
		//After this process you may change the best score array manually.
		if(score > best_score) {
			String output = "";
			//Writing simulation into output file
			output +=signed_libraries +"\n";
			
			for (OrderObject orderObject : library_order) {
				output += orderObject.id +" "+orderObject.books.size()+"\n";
				for (int i = 0; i < orderObject.books.size(); i++) 
					output += orderObject.books.get(i).id + " ";
				
				output += "\n";
			}
			
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		              new FileOutputStream(filename+".out"), "utf-8"))) {
		   writer.write(output);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	static ScanObject ScanBooks(Library lib,int days_left,int[] prev_scanned_books) {
		
		int score = 0;
		int book_index = 0;
		ArrayList<Book> books_scanned_now = new ArrayList<Book>();
		
		for (int i = 0; i < days_left; i++) {
			for (int j = 0; j < lib.ship_limit; j++) {
				
				while(book_index < lib.books.size() && 1 == prev_scanned_books[lib.books.get(book_index).id]  )
					book_index++;
				
				if(book_index < lib.books.size()) {
					books_scanned_now.add(lib.books.get(book_index));
					score += lib.books.get(book_index).score;
					prev_scanned_books[lib.books.get(book_index).id] = 1;
					book_index++;
				}
			}
			if(book_index == lib.num_books)
				break;
			
		}		
		return new ScanObject(score,prev_scanned_books,books_scanned_now);
		
	}

	
	//Reading files
	static void ReadDoc(String file) throws IOException {
		
		int bid = 0;
		int lid = 0;
		books.clear();
		libraries.clear();
		File openFile = new File(file+".txt");
		FileReader fr = new FileReader(openFile);
		BufferedReader br = new BufferedReader(fr);
		
		String line;// buffered reader
		line = br.readLine();
		String[] words = line.split(" ");
		book_count =  Integer.parseInt(words[0]);
		library_count =  Integer.parseInt(words[1]);
		day_count =  Integer.parseInt(words[2]);
		
		line = br.readLine();
		words = line.split(" ");
		for (int i = 0; i < words.length; i++) {
			books.add(new Book(bid,Integer.parseInt(words[i])));
			bid++;
		}
		
		int num_books = 0;
		int sign = 0;
		int ship = 0;
		try {

			while ((line = br.readLine()) != null) {
				
				words = line.split(" ");
				
				if(words.length < 2)
					break;
				
				num_books = Integer.parseInt(words[0]);
				sign = Integer.parseInt(words[1]);
				ship = Integer.parseInt(words[2]);
				
				//Temp library to add into arraylist
				Library temp = new Library(lid,num_books,sign,ship);
				lid++;
				
				line = br.readLine();
				words = line.split(" ");
				for (int i = 0; i < words.length; i++) 
					temp.books.add(books.get(Integer.parseInt(words[i])));	
				//Calculate librar's weight
				temp.weight  = CalculateWeight(temp);
				
				//Libraries's books is sorted by books score
				Collections.sort(temp.books, ScoreComparator);
				//Add library into arraylist
				libraries.add(temp);
				

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		fr.close();
		br.close();
	}
	
	//Calculate weight function for libraries
	static double CalculateWeight(Library lib) {
		int weight = 0;
		
		for (int i = 0; i < lib.books.size(); i++) 
			weight += lib.books.get(i).score;
		
		if(lib.sign_time == 0)
			return Integer.MAX_VALUE;
		
		else {
			return ( weight * Math.pow(lib.ship_limit,2)) / Math.pow(lib.sign_time, 2);
		}
				
	}
	
		//Comparator function for weights
	 static Comparator<Library> WeightComparator = new Comparator<Library>() {

			public int compare(Library l1, Library l2) {
			   double weight1 =  l1.weight;
			   double weight2 = l2.weight;
			   
			    return Double.compare(weight2, weight1)	  ;
		    }};
		//Comparator function for book scores
	 static Comparator<Book> ScoreComparator = new Comparator<Book>() {

			public int compare(Book b1, Book b2) {
			   int score1 = b1.score;
			   int score2 = b2.score;
			   			
			    return score2-score1;		  
		    }};
			
}

//Book Object
class Book {
	
	int score;
	int id;
	
	public Book(int id,int score) {
		this.id = id;
		this.score = score;
		
	}

}

//Scan object to return multiple variables
class ScanObject{
	int score=0;
	int[] prev_scanned_books ;
	ArrayList<Book> books_scanned_already = new ArrayList<Book>();
	
	
	public ScanObject(int score,int[] prev_scanned_books,ArrayList<Book> books_scanned_already) {
		this.score = score;
		this.prev_scanned_books =prev_scanned_books;
		this.books_scanned_already=books_scanned_already;
	}
	
	public ScanObject(){
		
	}
}


//Order object to keep Library_Order array. 
class OrderObject{
	int id;
	ArrayList<Book> books = new ArrayList<Book>();
	
	public OrderObject(int id,ArrayList<Book> books) {
		this.id = id;
		this.books = books;
	}
}

//Library object
class Library {
	int id;
	int num_books;
	int sign_time;
	int ship_limit;
	double weight= 0;
	
	ArrayList<Book> books = new ArrayList<Book>();
	
	public Library(int id,int books,int sign, int ship) {
		this.id = id;
		this.num_books = books;
		this.sign_time = sign;
		this.ship_limit = ship;		
	}

}
	
	


