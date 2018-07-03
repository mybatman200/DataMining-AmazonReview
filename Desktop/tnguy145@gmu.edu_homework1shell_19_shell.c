#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
/*
 * CS 475 HW1: Shell
 * http://www.jonbell.net/gmu-cs-475-spring-2018/homework-1/
 */


/*
AutoLab indicate that the program failed in memory check. However, as i run it against valgrind on Zeus
All functions except too-many-arguments sucessfully free all allocated memory 

too-many-arguments was also successfully ran on Zeus but it didn't get any point on autolab

*/

char **parse_line(char *line){ //function to parse the input

	int size =129; // max size allow +1
	int pos =0; //pos of each token
	char **string = malloc(size * sizeof(char*)); // create a pointer char
	char *token; //

	if(!string){ // check for emtry pointer
		fprintf(stderr, "error: %s\n", "allocation error");
		exit(1);
	}

	token = strtok(line, " \t\n"); // parse using tab and space delimiter
	while(token != NULL){ //add token to char pointer
		string[pos] = token;
		pos++;	
		token = strtok(NULL, " \t\n");
	}

	string[pos]= NULL;	 // clean up

	return string;
}

char *lsh_read_line(void)
{
  int size = 2048; // size of buffer
  int pos = 0;
  char *buffer = malloc(sizeof(char) * size); // buffer char pointer
  int c;

  if (!buffer) { //check for pointer error
    fprintf(stderr, "error: %s\n", "allocation error");
    exit(1);
  }

  do {
    if (size<pos) { // realloc if need more space 
      size *= 2;
      buffer = realloc(buffer, size);
      if (!buffer) {
        fprintf(stderr, "error: %s\n", "allocation error");
        exit(1);
      }
    }
    c = getchar(); //get character of input
    if(c== '\n'){ // handle enter button
    	buffer[pos] = '\0';
    	return buffer;
    }
    else if (c == EOF ) { //handle EOF
    	exit(1);
    }
    else if(c == '\n'){ 
    	buffer[pos] = '\0';
     	 return buffer;
    }
    else {
      buffer[pos] = c; // assign c to buffer pointer
    }
    pos++; //update the position
    
  }while(1);
 



}

void execute(char **args){ //execute shell without build in function
	
	pid_t rc=fork(); //fork
	
	if(rc==-1){ // check if child is not available
		fprintf(stderr, "error: %s\n", "No such file or directory");
		exit(1);
	}
	else if(rc==0){ //execute if child is available
		
		execvp(args[0],args);
		exit(1);
		//printf("args %s\n", args[1]);
	}
	else{
     waitpid(rc, NULL, 0);
		
	}


}
void cd_line(char **args)  // build-in command for cd 
{
  if (args[1] == NULL) { // cd requires 2 command, if one is missing, raise error
    fprintf(stderr, "error: expected argument to \"cd\"\n");
  } else {
    if (chdir(args[1]) != 0) {
      fprintf(stderr, "error: %s\n", strerror(errno));
    }
  }

}
int exit_line(void){ //exit build-in command exit the lopp
	return 0;
}

void history(char *hist[], int current){ //build-in command for history
	int i = 0;
	int num = 0;

	if(current >=99){ // if there are more than 100 command, look for the oldest one within 100
		i= current -100;
	}

	while(i<current){ // print the command if not NULL
		if(num<=99){			
				printf("%d: %s\n", num, hist[i]);
				num++;
				i++;				
		}
		else{
			break;
		}
		
	}
	
}



/*void *history_offset(char *hist[], int offset){
	char **off;

	if(offset>=100){
		fprintf(stderr, "error: %s\n", "offsset exceed limit");
	}
	else{

		off = parse_line(hist[offset]);
		execute(off);
		//printf("%s\n", off[0]);
	}

	
}*/



int main(int argc, char **argv) {
   char *line; // user input
   char **parse; // parse pointer
   int status=1; // loop status
   char *his[1000]; // history array

   int current=0;

	do{ //ends when status is at 0 or EOF
		printf("475$");
		fflush(stdout);
	
		line = lsh_read_line(); //readline from function
		 

		if(strcmp(line,"history")){ // compare line with history, if match then copy line to history
				his[current] = strdup(line);
				current++;		//keep track of history array
		}
					

		parse = parse_line(line); // parsing



		int x=0;
		while(parse[x]!=NULL){ // check if parse is full
			x++;
		}
		if(x<=0){
			continue;
		}
		else if(x>128){ //check if user exceed 128 arguments
			fprintf(stderr, "error: %s\n", "too many arguments");
			continue;
		}
		else{
		
		
		if(!strcmp(parse[0],"history")){ //handle history build=in function

			if(parse[1]!=NULL){ // check if it's -c or offset or just plain history
				if(!(strcmp(parse[1],"-c"))){ // clear history function
					current = 0;
					history(his, current);			
					continue;				
					}
				else { // offset function
					
					
					int sum = atoi(parse[1]); // convert to char to int to get the second token

					char **off;

						if(sum>=100){ //check if exceed the 100 limit
							fprintf(stderr, "error: %s\n", "offsset exceed limit");
						}
						else{ //
							if(current<100){
								off = parse_line(his[sum]);//convert array pointer to double pointer
								execute(off); //execute the function
							}
							else{
								sum = current-101;
								off = parse_line(his[sum]);
								execute(off);
							}
							
						}

				}
			}
			else{ //plain history 
				history(his,current);
				
			}

		}
		

		else if(!strcmp(parse[0],"cd")){ //handle cd build in
			cd_line(parse);
		}
		else if(!strcmp(parse[0],"exit")){ //handle exit build in 
			status = exit_line();
		}
		else{ //handle execution
			execute(parse);
		}

		
	}
	if(line!= NULL){
			free(line);
			
		}
		if(parse!=NULL){
			free(parse);
		}

		
	}while(status);


    exit(1);
    
}
