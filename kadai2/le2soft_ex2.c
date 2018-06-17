#include<sys/socket.h>
#include<netinet/in.h>
#include<netdb.h>
#include<unistd.h>
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<errno.h>

#define NUMSTR  3

char reqlines[NUMSTR][256];

int main(void)
{
	FILE *fp;
	char hostname[128];
	int i, s;
	struct hostent *hp;
	struct sockaddr_in sin;
	char buf[128];
	char url[200];
 	char *host, *port, *path;

	while(1){
	  printf("Input URL:\n");
	  fgets(url, 200, stdin);
	  
	  host = strtok(url, "//");
	  host = strtok(NULL, "/");
	  path = strtok(NULL, "\n");
	  
	  host = strtok(host, ":");
	  port = strtok(NULL, "\0");
	  
	  if(port == NULL)
	    port = "80";
	  
	  if ((hp = gethostbyname(host)) == NULL) {
	    fprintf(stderr, "%s: unknown host.\n", hostname);
	    exit(1);
	  }
	  
	  if((s = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
	    perror("client socket()");
	    exit(1);		
	  }
	  
	  bzero(&sin, sizeof(sin));
	  sin.sin_family = AF_INET;
	  sin.sin_port = htons(atoi(port));
	  memcpy(&sin.sin_addr, hp->h_addr, hp-> h_length);
	  
	  if (connect(s, (struct sockaddr *)&sin, sizeof(sin)) == -1) {
	    perror("client connect()");
	    exit(1);	
	  }
	  
	  fp = fdopen(s, "r");
	  
	  // send request to server
	  sprintf(reqlines[0], "GET /%s HTTP/1.1\r\n", path);
	  sprintf(reqlines[1], "Host: %s\r\n", host);
	  sprintf(reqlines[2], "\r\n");
	  for(i = 0; i < NUMSTR; i++) {
	    send(s, reqlines[i], strlen(reqlines[i]), 0);
	  }
	  
	  // receive contents from server
	  while (fgets(buf, sizeof(buf), fp) != NULL) {
	    printf("%s", buf);
	  }
	  
	  close(s);
	}
	return 0;
}

 
