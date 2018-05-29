package treasurehunt.util;

public class Matrix
{
	public static char[][] transpose(char[][] matrix)
	{
		char[][] transpose = new char[matrix[0].length][matrix.length];
		
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				transpose[j][i] = matrix[i][j];
			}
		}
		
		return transpose; 
	}
	
	public static char[][] verticalFlip(char[][] matrix)
	{
		char[][] flipped = new char[matrix[0].length][matrix.length];
		
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				flipped[i][matrix[i].length - j - 1] = matrix[i][j];
			}
		}
		
		return flipped; 
	}
	
	public static char[][] horizontalFlip(char[][] matrix)
	{
		char[][] flipped = new char[matrix[0].length][matrix.length];
		
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				flipped[matrix.length - i - 1][j] = matrix[i][j];
			}
		}
		
		return flipped; 
	}
}
