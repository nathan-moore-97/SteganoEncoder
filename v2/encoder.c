#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <arpa/inet.h>




int main(int argc, char *argv[]) {

    int mask = 2; // How much data of the image we will overwrite with the text file
    
    int img_fd, sec_fd, out_fd;
    int sec_nbytes, img_nbytes;
    if (argc != 3) {
        printf("Usage: ./encoder <PNG_FILE> <TEXT_FILE>\n");
        exit(1);
    }

    img_fd = open(argv[1], O_RDWR);
    sec_fd = open(argv[2], O_RDONLY);
    out_fd = open("result.png", O_CREAT | O_RDWR, (S_IRUSR | S_IWUSR));

    if (img_fd < 0) {
        printf("Error opening image file: %s\n", strerror(errno));
        exit(1);
    }
    
    if(sec_fd < 0) {
        printf("Error opening secret file: %s\n", strerror(errno));
        exit(1);
    }

    if(out_fd < 0) {
        printf("Error opening output file: %s\n", strerror(errno));
        exit(1);
    }

    sec_nbytes = lseek(sec_fd, 0, SEEK_END);
    img_nbytes = lseek(img_fd, 0, SEEK_END);

    lseek(sec_fd, 0, SEEK_SET);
    lseek(img_fd, 0, SEEK_SET);
    printf("Encoding %d bytes of \"%s\" into %d bytes of \"%s\"...\n", sec_nbytes, argv[2], img_nbytes, argv[1]);

    // Seek past the png header
    uint8_t header[8];
    read(img_fd, &header, sizeof(uint8_t) * 8);
    printf("PNG HEADER: ");
    for(int i = 0; i < 8; i++) {
        printf("%02X ", header[i]);
    }
    puts("");
    int cur_offset;
    int offset[INT16_MAX]; // TODO Do better than this.
    int num_offsets = 0; 
    int total_dat_len = 0;
    // Locate the data sections
    do {
        int32_t length;
        char type[5];
        read(img_fd, &length, sizeof(int32_t));
        read(img_fd, type, sizeof(uint32_t));
        length = ntohl(length);
        if (strcmp("IDAT", type) == 0) {
            offset[num_offsets++] = cur_offset;
            total_dat_len += length;
        } else {
            printf("SIZE: %5d%5s: %s\n", length, "TYPE", type);
        }
        cur_offset = lseek(img_fd, (length + 4), SEEK_CUR);
        
    } while(cur_offset < img_nbytes);

    printf("NUMBER OF DATA OFFSETS %d\n", num_offsets);
    // printf("Offset Location(s): ");
    // for(int i =0; i<num_offsets; i++) {
    //     printf("%d ", offset[i]);
    // }
    // puts("");
    printf("Total encodable data: %d\n", total_dat_len);
    
    
}