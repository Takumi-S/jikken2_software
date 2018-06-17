#include<sys/socket.h>
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<netinet/in.h>
#include<netdb.h>
#include<unistd.h>
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<errno.h>

int send_client(int fd, char *msg);

int main(int argc, char* argv[])
{
  FILE *fp;
  int s, ns, port, pid;
  struct sockaddr_in sin, fsin;
  char buf[1024+1];
  char method[128], uri[128], http_ver[128];
  char *uri_file;
  int read_fd;
  int len;
  
  socklen_t fromlen = sizeof(struct sockaddr_in);

  
  if((argc <= 1) || ((port = atoi(argv[1])) == 0)) {
    perror("no port number");
    exit(1);		
  }
  
  if((s = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
    perror("client socket()");
    exit(1);		
  }
  
  bzero(&sin, sizeof(sin));
  sin.sin_family = AF_INET;
  sin.sin_port = htons(port);
  sin.sin_addr.s_addr = INADDR_ANY;
  
  if (bind(s, (struct sockaddr *)&sin, sizeof(sin)) == -1) {
    perror("server bind()");
    exit(1);	
  }
  
  if (listen(s, 128) == -1) {
    perror("server listen()");
    exit(1);	
  }

  while(1){  
    if ((ns = accept(s, (struct sockaddr *)&fsin, &fromlen)) == -1) {
      perror("server accept()");	
      exit(1);
    }
    
    pid = fork();
    if(pid == 0){
      fp = fdopen(ns, "r");
      
      // receive requests from client (until an empty line)
      if(read(ns, buf, 1024) <= 0){
	perror("read()");
	exit(1);
      }else{
	sscanf(buf,"%s %s %s", method, uri, http_ver);
      }
      
      uri_file = uri + 1; // /を取る
      if((read_fd = open(uri_file, O_RDONLY, 0666)) == -1){
	send_client(ns,"HTTP/1.1 404 Not Found\r\n");
	send_client(ns,"Content-Type: text/html; charset=us-ascii\r\n");
	send_client(ns,"<HTML><HEAD>Not Found</HEAD>\r\n");
	send_client(ns,"<BODY>\r\n");
	send_client(ns,"The requested URL ");
	send_client(ns, uri);
	send_client(ns,"was not found on this server.\r\n");
	send_client(ns,"</BODY></HTML>\r\n");
      }
      else{
	send_client(ns,"HTTP/1.1 200 OK\r\n");
	send_client(ns,"Content-Type: text/html; charset=us-ascii\r\n");
	
	
	// send contents to client
	while((len = read(read_fd, buf, 1024)) > 0){
	  buf[len] = '\0';
	  send_client(ns, buf);
	  send_client(ns, "\n");
	}  
	close(ns);
	close(s);
	exit(0);
      }
    }else{
      close(ns);
    }
  }
  close(s);
  return 0;
}

int send_client(int fd, char *msg)
{
  int length;

  length = strlen(msg);

  if(write(fd, msg, length) != length){
    perror("write()");
    exit(1);
  }

  return length;
}


