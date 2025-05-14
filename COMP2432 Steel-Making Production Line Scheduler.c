#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>

#define MAX_ROWS 1000 // Maximun number of order can be received by this scheduler (Increased in stage 2 submission, oringinally settd as 100)
#define MAX_COLS 5
#define MAX_LENGTH 256

typedef struct {
    char* start;
    char* end;
}Period;

typedef struct { //separating date information
    int year;
    int month;
    int day;
}Date;

FILE *fptr;

char command[256]; //array for storing input
char* split_cmd[256]; //array for storing splitted command
char* split_order[MAX_ROWS][5];
char request;
char* cmd;
char* para1, para2, para3, para4, para5;
int i, j, k;

Period period;

// check if the year is a leap year
int isLeapYear(int year) {
    if (year % 400 == 0) return 1;
    if (year % 100 == 0) return 0;
    if (year % 4 == 0) return 1;
    return 0;
}

// convert the input date (string) to Date defined
// readDate is not equal to addPeriod() method.
Date readDate(char* date_string){
    //yyyy-mm-dd
    Date date;
    sscanf(date_string, "%d-%d-%d", &date.year, &date.month, &date.day);
    return date;
}

// convert number to string
char* itos(int num) {
    int temp = num;
    int length = 0;

    if (temp <= 0) {
        length = 1;  // negative or zero
    } else {
        while(temp != 0){ // length of int
            length++;
            temp /= 10;
        }
    }
    char* string = (char*)malloc((length + 1) * sizeof(char));

    // Convertion
    int i = 0;
    if (num < 0){ // negative number
        string[0] = '-';
        num = -num;
        i++;
    } else if (num == 0){
        string[0] = '0';
        i++;
    }
    while (num != 0) {
        int digit = num % 10;
        string[length - i - 1] = '0' + digit;
        num /= 10;
        i++;
    }
    string[length] = '\0';  // end of character
    return string;
}

// Function to check whether the date is valid, return 1 means valid, 0 means invalid
int validDate(Date date) {
    if(date.month == 1 || date.month == 3 || date.month == 5 || date.month == 7 || date.month == 8 || date.month == 10 || date.month == 12) {
        if (date.day > 31 || 0 >= date.day) return 0;
    } else if(date.month == 4 || date.month == 6 || date.month == 9 || date.month == 11){
        if (date.day > 30 || 0 >= date.day) return 0;
    } else if(date.month == 2) {
        // Check whether the year is leap year
        if ((date.year % 4 == 0 && date.year % 100 != 0) || (date.year % 400 == 0)) {
            if (date.day > 29 || 0 >= date.day) return 0;
        } else {
            if (date.day > 28 || 0 >= date.day) return 0;
        }
    } else return 0;
    return 1; // Valid date
}

int checkOrder(Date due_date, char* quantity, Date s_date, Date e_date, char* productName){
    // check whether the due date is valid
    if (validDate(due_date) == 0) {
        return 1;
    }
    // check whether the due date is out of period
    if (e_date.year < due_date.year ||
    (e_date.year == due_date.year && e_date.month < due_date.month) || 
    (e_date.year == due_date.year && e_date.month == due_date.month && e_date.day < due_date.day) ||
    s_date.year > due_date.year || 
    (s_date.year == due_date.year && s_date.month > due_date.month) || 
    (s_date.year == due_date.year && s_date.month == due_date.month && s_date.day > due_date.day)) {
        return 2;
    }
    int j = 0;
    while (quantity[j] != '\0'){
        if (!isdigit(quantity[i])){
            return 3;
        }
        j++;
    }
    // check whether the product name is valid
    char productType = productName[8];
    if (strncmp(productName, "Product_", 8) != 0 || strlen(productName) != 9 || productType < 'A' || productType > 'I') {
        return 4;
    }
    return 0;
}

// Function to add days to a given date
char* dateCalculator(const char* start_date, int numDays) {
    int i;
    Date date;
    sscanf(start_date, "%d-%d-%d", &date.year, &date.month, &date.day);
    
    int daysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    Date target = date;
    
    while (numDays > 0) {
        int monthDays = daysInMonth[target.month - 1];
        if (target.month == 2 && isLeapYear(target.year)) {
            monthDays = 29;
        }
        int daysLeftInMonth = monthDays - target.day + 1;
        if (numDays <= daysLeftInMonth) {
            target.day += numDays;
            break;
        } else {
            numDays -= daysLeftInMonth;
            target.day = 1;
            target.month++;
            if (target.month > 12) {
                target.month = 1;
                target.year++;
            }
        }
    }
    char* targetDateStr = (char*)malloc(11 * sizeof(char));  // Allocate memory for the target date string (YYYY-MM-DD + '\0')
    sprintf(targetDateStr, "%d-%02d-%02d", target.year, target.month, target.day);
    return targetDateStr;
}


// Function to calculate the number of days in a month
int countDays(int month, int year) {
    int allDays = 0, currentMonth = 1, currentYear = 1;

    // Add days for all months before the given month
    for (currentMonth = 1; currentMonth < month; currentMonth++) {
        if(currentMonth == 1 || currentMonth == 3 || currentMonth == 5 || currentMonth == 7 || currentMonth == 8 || currentMonth == 10 || currentMonth == 12) {
            allDays += 31;
        } else if(currentMonth == 4 || currentMonth == 6 || currentMonth == 9 || currentMonth == 11){
            allDays += 30;
        } else if(currentMonth == 2) {
            // Check whether the year is leap year
            if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                allDays += 29;
            } else {
                allDays += 28;
            }
        }
    }
    // Add days for all years before the given year
    for (currentYear = 1; currentYear < year; currentYear++) {
        // Check whether the year is leap year
        if ((currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0)) {
            allDays += 366;
        } else {
            allDays += 365;
        }
    }
    return allDays;
}

// Function to calculate the number of days between two dates
int daysCalculation(Date d1, Date d2) {
    int days = d2.day + countDays(d2.month, d2.year) - d1.day - countDays(d1.month, d1.year);
    return days+1;
}

void addORDER(char* cmd, char* order_name, char* due_date, char* quantity, char* product_name){
    FILE* file;

    // Open the file in append mode
    file = fopen("tempORDER.txt", "a");

    // Check if the file was opened successfully
    if (file != NULL) {
        // Write the order to the file
        fprintf(file, "%s %s %s %s %s\n", cmd, order_name, due_date, quantity, product_name);

        // Close the file
        fclose(file);
    } else {
        // Print an error message if the file cannot be opened
        printf("Unable to open the file.\n");
    }
}

void addBATCH (const char* batchfileName,  Date s_date, Date e_date){
    FILE* orderFile;
    FILE* batchFile;
    char order[MAX_LENGTH];

    // Open the .dat file
    batchFile = fopen(batchfileName, "rb");
    if (batchFile == NULL){
        printf("Error opening file\n");
        // fclose(orderFile);
        return;
    }
    
    char* input;
    char* split_order[256];
    char* errorOrderNumber[MAX_ROWS];
    int countErrorInput = 0, i = 0;
    int co = 0;
    int j = 0;
    // Put the orders into .txt file
    while (fgets(order, sizeof(order), batchFile) != NULL){
        if (order[strlen(order)-1] == '\n'){
            order[strlen(order)-1] = '\0';            
        }
        input = strtok(order, " ");
        for (i=0; input!=NULL; i++){ // split the order and store the words into new array
            split_order[i] = malloc(strlen(input) + 1);
            strcpy(split_order[i], input);
            input = strtok(NULL," ");
        }
        
        co = checkOrder(readDate(split_order[2]), split_order[3], s_date, e_date, split_order[4]);
        if (co > 0){
            errorOrderNumber[countErrorInput++] = split_order[1];
        } else if (co == 0){
            addORDER(split_order[0], split_order[1], split_order[2], split_order[3], split_order[4]);
        }    
    }

    // Close all the file
    fclose(batchFile);

    if (countErrorInput > 0){
        printf("There are %d error input in the provided batch file. Here is the list of order number of the error input:\n",countErrorInput);
        for (i = 0; i < countErrorInput; i++){
            printf("%s\n",errorOrderNumber[i]);
        }
    }
}

void printREPORT(char report_file_name[100], char* algorithm, char* acceptedOrder[MAX_ROWS][6], char* rejectedOrder[MAX_ROWS][4], int acceptedNum, int rejectedNum, int totalDay, int dayX, int dayY, int dayZ){
    int i = 0;
    int quantityX = 0, quantityY = 0, quantityZ = 0;
    // calculate the sum of products produced of each plant
    for (i = 0; i < acceptedNum; i++){ 
        if (strcmp(acceptedOrder[i][5], "Plant_X") == 0){
            quantityX += atoi(acceptedOrder[i][4]);
        } else if (strcmp(acceptedOrder[i][5], "Plant_Y") == 0){
            quantityY += atoi(acceptedOrder[i][4]);
        } else if (strcmp(acceptedOrder[i][5], "Plant_Z") == 0){
            quantityZ += atoi(acceptedOrder[i][4]);
        }
    }

    // Utiliazation calculation
    double utilizationX;
    double utilizationY;
    double utilizationZ;
    double utilizationX2;
    double utilizationY2;
    double utilizationZ2;
    double overallUtilization;
    double overallUtilization2;

    utilizationX = (double)quantityX/(totalDay*300) * 100;
    utilizationY = (double)quantityY/(totalDay*400) * 100;
    utilizationZ = (double)quantityZ/(totalDay*500) * 100;

    utilizationX2 = (double)quantityX/(dayX*300) * 100;
    utilizationY2 = (double)quantityY/(dayY*400) * 100;
    utilizationZ2 = (double)quantityZ/(dayZ*500) * 100;
    overallUtilization2 = (double) (utilizationX2+utilizationY2+utilizationZ2)/3;
    overallUtilization = (double) (quantityX+quantityY+quantityZ)/(totalDay*1200) *100;

    // Report writing
    FILE* reportFile;
    char algoTemp[50];
    if (strcmp(algorithm, "FCFS")==0) {
        strcpy(algoTemp, "First Come First Served");
    } else if (strcmp(algorithm, "SJF")==0) {
        strcpy(algoTemp, "Shortest Job First");
    }
    reportFile = fopen(report_file_name, "w"); // Create a file for report writing
    if (reportFile == NULL) {
        printf("Error creating file\n");
        return;
    }
    fprintf(reportFile,"***PLS Schedule Analysis Report***\n\n");
    fprintf(reportFile,"Algorithm used: %s\n\n", algoTemp);
    fprintf(reportFile,"There are %d Orders ACCEPTED. Details are as follows: \n",acceptedNum);
    fprintf(reportFile, "\n%-20s %-20s %-20s %-20s %-20s %-20s", "ORDER NUMBER", "START", "END", "DAYS", "QUANTITY", "PLANT");
    fprintf(reportFile,"\n==================================================================================================================\n");
    for (i = 0; i < acceptedNum; i++){
        fprintf(reportFile, "%-20s %-20s %-20s %-20s %-20s %-20s\n", acceptedOrder[i][0],acceptedOrder[i][1],acceptedOrder[i][2],acceptedOrder[i][3],acceptedOrder[i][4],acceptedOrder[i][5]);
    }
    fprintf(reportFile,"\n");
    fprintf(reportFile,"%30s"," - End - \n\n");
    fprintf(reportFile,"==================================================================================================================\n\n");
    fprintf(reportFile,"\nThere are %d Orders REJECTED. Details are as follows: \n\n", rejectedNum);
    fprintf(reportFile, "%-20s %-20s %-20s %-20s\n", "ORDER NUMBER", "PRODUCT NAME", "Due Date","QUANTITY");
    fprintf(reportFile,"==================================================================================================================\n");
    for (i = 0; i < rejectedNum; i++){
        fprintf(reportFile, "%-20s %-20s %-20s %-20s\n",rejectedOrder[i][0],rejectedOrder[i][1],rejectedOrder[i][2],rejectedOrder[i][3]);
    }
    fprintf(reportFile,"\n");
    fprintf(reportFile,"%30s %s"," - End - ", "\n\n==================================================================================================================\n\n\n");
    fprintf(reportFile,"***PERFORMANCE\n\nPlant_X:\n");
    fprintf(reportFile,"        Number of days in use:%*d days\n",39,dayX);
    fprintf(reportFile,"        Number of products produced:%*d (in total)\n",35,quantityX);
    fprintf(reportFile,"        Utilization of the plant (within the period):%*.1f %% \n",18,utilizationX);
    fprintf(reportFile,"        Utilization of the plant (within the days in used):%*.1f %% \n\n",12,utilizationX2);
    fprintf(reportFile,"Plant_Y:\n");
    fprintf(reportFile,"        Number of days in use:%*d days\n",39,dayY);
    fprintf(reportFile,"        Number of products produced:%*d (in total)\n",35,quantityY);
    fprintf(reportFile,"        Utilization of the plant (within the period):%*.1f %% \n",18,utilizationY);
    fprintf(reportFile,"        Utilization of the plant (within the days in used):%*.1f %% \n\n",12,utilizationY2);
    fprintf(reportFile,"Plant_Z:\n");
    fprintf(reportFile,"        Number of days in use:%*d days\n",39,dayZ);
    fprintf(reportFile,"        Number of products produced:%*d (in total)\n",35,quantityZ);
    fprintf(reportFile,"        Utilization of the plant (within the period):%*.1f %% \n",18,utilizationZ);
    fprintf(reportFile,"        Utilization of the plant (within the days in used):%*.1f %% \n\n",12,utilizationZ2);

    fprintf(reportFile,"Overall utilization (within the period):%*.1f %% \n",31,overallUtilization);
    fprintf(reportFile,"Overall utilization (within the days in used):%*.1f %%",25,overallUtilization2);
   
    // Close the file
    fclose(reportFile);
}


int main(){
    int parent = getpid();
    int child;
    int pid;
    int i,j;
    bool running = true;
    char* fileName = {"tempOrder.txt"};
    char* batchfileName;
    char buf[14096];

    Date s_date; //start period date
    Date e_date; //end period date

    int fd[4][2]; 
    /*[4]:0.input -> FCFS 1. FCFS -> parent 2. input -> SJF 3.SJF -> parent  (2 pairs of pipes)
      [2]:0.read 1.write
    */
    for(i = 0;i < 4; i++){
        if(pipe(fd[i]) < 0){ // create pipes
            printf("Pipe creation error.\n");
            exit(1);
        }
    }
    pid = fork(); // create child 1
    if(getpid()==parent) {  // create child 2 by parent
        pid = fork();
    } 
    if(pid < 0){
        printf("Fork failed.\n");
        exit(1);
    }
    /* Parent process: input of user interface
       Child process1: FCFS scheduler
       Child process2: SJF scheduler
    */
    else if(pid == 0){ // child process
        child = getpid() - parent;
        //Child process1: FCFS
        if(child == 1){
            close(fd[0][1]);close(fd[1][0]);
            char buf1[2048];
            char* split_buf1[2048];
            while(true) { // continuously reading message from pipe to see if parent sends any message to child 1
                read(fd[0][0], buf1, sizeof(buf1));
                if (strcmp(buf1,"work")==0) { // start child 1's operation
                    read(fd[0][0], buf1, sizeof(buf1)); // get the period dates and report file name from parent
                    // FCFS algorithm
                    char* input = strtok(buf1," ");
                    for (i=0; input!=NULL; i++){ //split the command and store the words into new array
                        split_buf1[i] = malloc(strlen(input) + 1);
                        split_buf1[i] = input;
                        input = strtok(NULL," ");
                    }

                    char reportFile[100];
                    strcpy(reportFile, split_buf1[2]);
                    Date start = readDate(split_buf1[0]);
                    Date end = readDate(split_buf1[1]);
                    char* start_date = split_buf1[0];
                    char* s_date;
                    char* e_date;
                    FILE *file;
                    char* token;
                    char line[MAX_LENGTH];
                    int n = 0;
                    char orders[MAX_ROWS][MAX_LENGTH];
                    char orders2[MAX_ROWS][MAX_LENGTH];
                    char* split_order[MAX_ROWS][10];
                    int i,j,k;
                    int row = 0;
                    int store[1000];
                    int today = 0;
                    int due = daysCalculation(start,end);
                    int dueDay[3] = {due,due,due}; // Due day for each plant depend on input
                    int plant[3] = {0,0,0};  // Which means the order of Plant_X,Y,Z
                    int workload[3] = {300,400,500};
                    int requiredPeriod;
                    int day; // how many days to complete the order(come from addperiod)
                    file = fopen(fileName, "r");
                    if (file == NULL) {
                        printf("Error opening file.\n");
                        return 1;
                    }
                    while (fgets(line, sizeof(line), file)) {
                        line[strcspn(line, "\n")] = '\0'; // Remove the newline character from the line
                        strcpy(orders[row], line); // Copy file contents into the order array
                        strcpy(orders2[row], line);
                        row++;
                    }
                    for (j = 0; j < MAX_ROWS;j++){
                        input = strtok(orders2[j], " ");
                        for (i=0; input!=NULL; i++){ //split the command and store the words into new array
                            split_order[j][i] = malloc(strlen(input) + 1);
                            strcpy(split_order[j][i], input);
                            input = strtok(NULL," ");
                        }
                    }
                    fclose(file);

                    //split_order[i][0]: addORDER
                    //split_order[i][1]: order_number
                    //split_order[i][2]: due_date
                    //split_order[i][3]: quantity
                    //split_order[i][4]: product_name
                    //split_order[i][5]: recieve_status (1:accept / 0:reject)
                    //split_order[i][6]: Plant_X/Y/Z
                    //split_order[i][7]: end_date
                    //split_order[i][8]: start_date
                    //store[i]: how many days for actual work

                    // i = which order is producing currently
                    for(i = 0; i < row; i++){
                        Date finishDay = readDate(split_order[i][2]);
                        day = daysCalculation(start,finishDay);
                        int quantity = atoi(split_order[i][3]);
                        while(plant[0] != 0 && plant[1] != 0 && plant[2] !=0){
                        // day of plant x producing product pass 1 day, plant[0]-1
                        plant[0] = plant[0] - 1;
                        plant[1] = plant[1] - 1;
                        plant[2] = plant[2] - 1;
                        today++;
                        }
            
                        for(j = 0; j < 3; j++){
                            if(plant[j] == 0){
                                if(j == 0){
                                    // if the workload of plant (not) enough for the quantity of order
                                    if(quantity % workload[j] == 0){
                                        requiredPeriod = quantity / workload[j];
                                    } 
                                    if(quantity % workload[j] != 0){
                                        requiredPeriod = (quantity / workload[j]) + 1;
                                    }
                                    if(requiredPeriod > (day - today)){ // cannot finish before due date
                                        split_order[i][5] = "0";
                                        // status = 0 = order is idle -> go to next plant to see if next plant accept it, if not, reject this or
                                    }
                                    // if plant accept the order
                                    else if(requiredPeriod <= (day - today) && requiredPeriod <= dueDay[j]){
                                        dueDay[j] = dueDay[j] - requiredPeriod;
                                        plant[j] = requiredPeriod; // how many days require plant to finish the order?
                                        split_order[i][5] = "1"; // Accepted
                                        split_order[i][6] = "Plant_X";
                                        char str[1000];
                                        store[i]=requiredPeriod;
                                        s_date = dateCalculator(start_date,today);
                                        split_order[i][8] = s_date;
                                        e_date = dateCalculator(s_date,(requiredPeriod - 1));
                                        split_order[i][7] = e_date;
                                        break;
                                    }
                                    else{
                                        split_order[i][5] = "0";
                                    }
                                }
                                if(j == 1){
                                    if(quantity % workload[j] == 0){
                                        requiredPeriod = quantity / workload[j];  
                                    }
                                    if(quantity % workload[j] != 0){
                                        requiredPeriod = (quantity / workload[j]) + 1;
                                    }
                                    if(requiredPeriod > (day - today)){ // cannot finish before due date
                                        split_order[i][5] = "0";
                                        // status = 0 = order is idle -> go to next plant to see if next plant accept it, if not, reject this or
                                    }
                                    else if(requiredPeriod <= (day - today) && requiredPeriod <= dueDay[j]){ // cannot finish before due date
                                        dueDay[j] = dueDay[j] - requiredPeriod;
                                        plant[j] = requiredPeriod;
                                        split_order[i][5] = "1";
                                        split_order[i][6] = "Plant_Y";
                                        char str[1000];
                                        store[i]=requiredPeriod;
                                        s_date = dateCalculator(start_date,today);
                                        split_order[i][8] = s_date;
                                        e_date = dateCalculator(s_date,(requiredPeriod - 1));
                                        split_order[i][7] = e_date;
                                        // status = 0 = order is idle -> go to next plant to see if next plant accept it, if not, reject this or
                                        break;
                                    }
                                    else{
                                        split_order[i][5] = "0";
                                    }
                                }
                                if(j == 2){
                                    if(quantity % workload[j] == 0){
                                        requiredPeriod = quantity / workload[j];  
                                    }
                                    if(quantity % workload[j] != 0){
                                        requiredPeriod = (quantity / workload[j]) + 1;
                                    }
                                    if(requiredPeriod > (day - today)){ // cannot finish before due date
                                        split_order[i][5] = "0";
                                        // status = 0 = order is idle -> go to next plant to see if next plant accept it, if not, reject this or
                                    }
                                    else if(requiredPeriod <= (day - today) && requiredPeriod <= dueDay[j]){ // cannot finish before due date
                                        dueDay[j] = dueDay[j] - requiredPeriod;
                                        plant[j] = requiredPeriod;
                                        split_order[i][5] = "1";
                                        split_order[i][6] = "Plant_Z";
                                        char str[1000];
                                        store[i]=requiredPeriod;
                                        s_date = dateCalculator(start_date,today);
                                        split_order[i][8] = s_date;
                                        e_date = dateCalculator(s_date,(requiredPeriod - 1));
                                        split_order[i][7] = e_date;
                                        // status = 0 = order is idle -> go to next plant to see if next plant accept it, if not, reject this or
                                        break;
                                    }
                                    else{
                                        split_order[i][5] = "0";
                                    }
                                }
                            }
                        }
                    }

                    //accept status i define as split_order[i][5],when = 1 accept and 0 reject
                    char* acceptedOrder[MAX_ROWS][6]; // it is for putting into buffer in same order as child 2|okkkk
                    int acceptedNum = 0;
                    for(i = 0; i < row; i++){
                        if(strcmp(split_order[i][5],"0")){
                            acceptedOrder[acceptedNum][0] = split_order[i][1]; // order_number
                            acceptedOrder[acceptedNum][1] = split_order[i][8]; // start_date
                            acceptedOrder[acceptedNum][2] = split_order[i][7]; // end_date 
                            acceptedOrder[acceptedNum][3] = itos(store[i]); // duration (days used in Plant for production)
                            acceptedOrder[acceptedNum][4] = split_order[i][3]; // quantity
                            acceptedOrder[acceptedNum][5] = split_order[i][6];  // plantX/Y/Z
                            acceptedNum++;
                        }
                    }
                    char* rejectedOrder[MAX_ROWS][4];
                    int rejectedNum = 0;
                    for(i = 0; i < row; i++){
                        if(strcmp(split_order[i][5],"1")){
                            rejectedOrder[rejectedNum][0] = split_order[i][1]; // order number
                            rejectedOrder[rejectedNum][1] = split_order[i][4];  // product name
                            rejectedOrder[rejectedNum][2] = split_order[i][2]; // due_date
                            rejectedOrder[rejectedNum][3] = split_order[i][3];  // quantity  
                            rejectedNum++;
                        }    
                    }
                    int dayX = due - dueDay[0]; // calculate number of working days per plant
                    int dayY = due - dueDay[1]; 
                    int dayZ = due - dueDay[2]; 
                    printREPORT(reportFile, "FCFS", acceptedOrder, rejectedOrder, acceptedNum, rejectedNum, due, dayX, dayY, dayZ);
                    strcpy(buf,"FCFSfinish"); // indicate child 1 has finished its operation
                    write(fd[1][1], buf, sizeof(buf));
                }
                else if (strcmp(buf1,"done")==0) { // terminate child 1
                    close(fd[0][0]);close(fd[1][1]);
                    exit(0);
                }
            }
        }
        // Child process2: SJF
        if(child ==2){
            close(fd[2][1]);close(fd[3][0]);
            char buf2[2048];
            char* split_buf2[2048];
            while(true) { // continuously reading message from pipe to see if parent sends any message to child 2
                read(fd[2][0], buf2, sizeof(buf2));
                if (strcmp(buf2,"work")==0) { // start child 2's operations
                    
                    read(fd[2][0], buf2, sizeof(buf2));
                   
                    char* input = strtok(buf2, " "); 
                    for (i=0; input!=NULL; i++){ //split the command and store the words into new array
                        
                        split_buf2[i] = malloc(strlen(input) + 1);
                        split_buf2[i] = input;
                        input = strtok(NULL," ");
                    }

                    // SJF algorithm
                    char orders[MAX_ROWS][MAX_LENGTH];
                    char orders2[MAX_ROWS][MAX_LENGTH];
                    char* split_order[MAX_ROWS][MAX_COLS];
                    char* sorted_order[MAX_ROWS][MAX_COLS];
                    FILE* file;
                    char line[MAX_LENGTH];
                    char reportFile[100];
                    strcpy(reportFile, split_buf2[2]);

                    s_date = readDate(split_buf2[0]);
                    e_date = readDate(split_buf2[1]);

                    // open the tempOrder.txt file
                    file = fopen(fileName, "r");
                    if (file == NULL) {
                        printf("Error opening file.\n");
                        return 1;
                    } 
                    int row = 0;
                    while (fgets(line, sizeof(line), file)) {
                        line[strcspn(line, "\n")] = '\0'; // Remove the newline character from the line
                        strcpy(orders[row], line); // Copy file contents into the order array
                        strcpy(orders2[row], line);
                        row++;
                    }

                    for (j = 0; j < MAX_ROWS;j++){
                        char* input = strtok(orders2[j], " ");
                        for (i = 0; input!=NULL; i++){  // split the command and store the words into new array
                            split_order[j][i] = malloc(strlen(input) + 1);
                            strcpy(split_order[j][i], input);
                            input = strtok(NULL," ");
                        }
                    }
                    fclose(file);

                    // Sort the orders by quantity in ascending order
                    char temp[MAX_LENGTH];
                    for (i = 1; i < row; i++){
                        for (j = 0; j < row-i; j++){
                            if (atoi(split_order[j+1][3]) < atoi(split_order[j][3])){ 
                                strcpy(temp, split_order[j][3]);
                                strcpy(split_order[j][3], split_order[j+1][3]);
                                strcpy(split_order[j+1][3], temp);
                                strcpy(temp, orders[j]);
                                strcpy(orders[j], orders[j+1]);
                                strcpy(orders[j+1], temp);
                            }
                        }
                    }

                    for (j = 0; j < MAX_ROWS;j++){
                        char* input = strtok(orders[j], " ");
                        for (i=0; input!=NULL; i++){ //split the sorted orders and store the words into new array
                            sorted_order[j][i] = malloc(strlen(input) + 1);
                            strcpy(sorted_order[j][i], input);
                            input = strtok(NULL," ");
                        }
                    }
                    
                    // Check whether the order should be accepted or rejected
                    /*
                    1. plant choosing: i%3 -> 0 = X, 1 = Y, 2 = Z, i++ after each loop
                    2. workload before due date = (due - s_date) * workload of plant(300/400/500)
                        -> 1st order: s_date = start period
                        -> remaining: s_date = end date of previous order
                    3. days required = quantity/workload (have remainder + 1)
                    4. if workload < quantity -> reject
                    */
                    int acceptedNum = 0; // Number of accepted orders
                    int rejectedNum = 0; // Number of rejected order
                    int workload[3] = {300,400,500}; // Workload of plant chosen = workload[i%3]
                    char* acceptedOrder[MAX_ROWS][6];
                    char* rejectedOrder[MAX_ROWS][4];
                    char* start_dateX = split_buf2[0];
                    char* start_dateY = split_buf2[0];
                    char* start_dateZ = split_buf2[0];
                    char* end_dateX;
                    char* end_dateY;
                    char* end_dateZ;
                    int total_quantity = 0; // sum of quantity of all plants during the period (300+400+500)*day_required
                    int total_workloadX = 0; // Total workload of the plant in before the due date = days*workload of plant chosen
                    int total_workloadY = 0;
                    int total_workloadZ = 0;
                    int totalDay = daysCalculation(s_date, e_date);
                    int day_required = 0;
                    int dayX = 0; // total number of days used the plant
                    int dayY = 0;
                    int dayZ = 0;
                    char* day_r = "0";

                    for (i = 0; i < row; i++){
                        Date orderDue = readDate(sorted_order[i][2]);
                        day_required = daysCalculation(s_date, orderDue); // due date - start date, the day that products can finished during this period
                        int workingDays = 0;
                        if(day_required < 0){ // reject order
                            rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                            rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                            rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                            rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                            rejectedNum++;
                        } else if(day_required > 0){ // Can this order finished before due date
                            if(i%3 == 0){ // plant x
                                total_workloadX += day_required * workload[0];
                                if (atoi(sorted_order[i][3])%workload[0] > 0){ // calculate the number of days required to finish the order = working Days
                                    workingDays = atoi(sorted_order[i][3]) / workload[0] + 1;
                                } else workingDays = atoi(sorted_order[i][3]) / workload[0];
                                if (total_workloadX >= atoi(sorted_order[i][3])){ // Accepted when plant X have enough space to produce this order
                                    char* end_dateX = dateCalculator(start_dateX, workingDays-1);
                                    if (daysCalculation(readDate(end_dateX),e_date) <= 0 && daysCalculation(readDate(start_dateX), e_date) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadX = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(end_dateX),orderDue) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadX = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(start_dateX),orderDue) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadX = 0;
                                        continue;
                                    // } else if (daysCalculation(readDate(start_dateX),orderDue) == 1 && atoi(sorted_order[i][3]) > workload[0]){
                                    //     rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    //     rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    //     rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    //     rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    //     rejectedNum++;
                                    //     total_workloadX = 0;
                                    //     continue;
                                    } else if (daysCalculation(readDate(end_dateX),e_date)>0 && daysCalculation(readDate(end_dateX),orderDue)>0 && daysCalculation(readDate(start_dateX),orderDue)>0){
                                        acceptedOrder[acceptedNum][0] = sorted_order[i][1]; // order number
                                        acceptedOrder[acceptedNum][1] = start_dateX; // start date
                                        acceptedOrder[acceptedNum][2] = end_dateX; // end date
                                        day_r = itos(workingDays);
                                        acceptedOrder[acceptedNum][3] = day_r; // number of days used
                                        acceptedOrder[acceptedNum][4] = sorted_order[i][3]; // quantity
                                        acceptedOrder[acceptedNum][5] = "Plant_X";
                                        acceptedNum++;
                                        dayX += workingDays;
                                    }
                                    if (i + 3 < row){
                                        if (strcmp(sorted_order[i][4], sorted_order[i+3][4]) == 0){ // the next order is producing the same product
                                            start_dateX = end_dateX; // startdate of next order = end date of previous order
                                            total_workloadX = total_workloadX - atoi(sorted_order[i][3]); // remaining workload after finishing the order
                                        } else {
                                            start_dateX = dateCalculator(end_dateX, 1);
                                            total_workloadX = 0;
                                        }
                                    }
                                } else { //reject
                                    rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    rejectedNum++;
                                    total_workloadX = 0;
                                }
                            }else if(i%3 == 1){ // plant Y
                                total_workloadY += day_required * workload[1];
                                if (atoi(sorted_order[i][3])%workload[1] > 0){ // calculate the number of days required to finish the order
                                    workingDays = atoi(sorted_order[i][3]) / workload[1] + 1;
                                } else workingDays = atoi(sorted_order[i][3]) / workload[1];
                                if (total_workloadY >= atoi(sorted_order[i][3])){ // Accepted by plant Y
                                    end_dateY = dateCalculator(start_dateY, workingDays-1);
                                    if (daysCalculation(readDate(end_dateY),e_date) <= 0 && daysCalculation(readDate(start_dateY), e_date) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadY = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(end_dateY),orderDue) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadY = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(start_dateY),orderDue) <= 0){ //order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadX = 0;
                                        continue;
                                    // } else if (daysCalculation(readDate(start_dateY),orderDue) == 1 && atoi(sorted_order[i][3]) > workload[2]){
                                    //     rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    //     rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    //     rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    //     rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    //     rejectedNum++;
                                    //     total_workloadX = 0;
                                    //     continue;
                                    } else if(daysCalculation(readDate(end_dateY),e_date)>0 && daysCalculation(readDate(end_dateY),orderDue)>0 && daysCalculation(readDate(start_dateY),orderDue)>0){
                                        acceptedOrder[acceptedNum][0] = sorted_order[i][1]; // store the order number into accepted array
                                        acceptedOrder[acceptedNum][1] = start_dateY;
                                        acceptedOrder[acceptedNum][2] = end_dateY;
                                        day_r = itos(workingDays);
                                        acceptedOrder[acceptedNum][3] = day_r;
                                        acceptedOrder[acceptedNum][4] = sorted_order[i][3]; // quantity
                                        acceptedOrder[acceptedNum][5] = "Plant_Y";
                                        acceptedNum++;
                                        dayY += workingDays;
                                    }
                                    if (i + 3 < row){
                                        if (strcmp(sorted_order[i][4], sorted_order[i+3][4]) == 0){ // the next order is producing the same product
                                            start_dateY = end_dateY;
                                            total_workloadY = total_workloadY - atoi(sorted_order[i][3]); 
                                        } else {
                                            start_dateY = dateCalculator(end_dateY, 1);
                                            total_workloadY = 0;
                                        }
                                    }
                                } else { //reject
                                    rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    rejectedNum++;
                                    total_workloadY = 0;
                                }
                            }else if(i%3 == 2){ // plant Z
                                total_workloadZ += day_required * workload[0];
                                if (atoi(sorted_order[i][3])%workload[2] > 0){ // calculate the number of days required to finish the order
                                    workingDays = atoi(sorted_order[i][3]) / workload[2] + 1;
                                } else workingDays = atoi(sorted_order[i][3]) / workload[2];
                                if (total_workloadZ >= atoi(sorted_order[i][3])){ // Accepted
                                    end_dateZ = dateCalculator(start_dateZ, workingDays-1);
                                    if (daysCalculation(readDate(end_dateZ),e_date) <= 0 && daysCalculation(readDate(start_dateZ), e_date) <= 0){ // order rejected as it cannot finsih before the end of period
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadZ = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(end_dateZ),orderDue) <= 0){ //order rejected as it cannot finsih before the end date of order
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadZ = 0;
                                        continue;
                                    } else if (daysCalculation(readDate(start_dateZ),orderDue) <= 0){ //order rejected as it cannot finsih before the start date of order
                                        rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                        rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                        rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                        rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                        rejectedNum++;
                                        total_workloadZ = 0;
                                        continue;
                                    // } else if (daysCalculation(readDate(start_dateZ),orderDue) == 1 && atoi(sorted_order[i][3]) > workload[2]){
                                    //     rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    //     rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    //     rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    //     rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    //     rejectedNum++;
                                    //     total_workloadX = 0;
                                    //     continue;
                                    } else if(daysCalculation(readDate(end_dateZ),e_date)>0 && daysCalculation(readDate(end_dateZ),orderDue)>0 && daysCalculation(readDate(start_dateZ),orderDue)>0){
                                        acceptedOrder[acceptedNum][0] = sorted_order[i][1]; // order number
                                        acceptedOrder[acceptedNum][1] = start_dateZ; 
                                        acceptedOrder[acceptedNum][2] = end_dateZ;
                                        day_r = itos(workingDays);
                                        acceptedOrder[acceptedNum][3] = day_r;
                                        acceptedOrder[acceptedNum][4] = sorted_order[i][3];
                                        acceptedOrder[acceptedNum][5] = "Plant_Z";
                                        acceptedNum++;
                                        dayZ += workingDays;
                                    }
                                    if (i + 3 < row){
                                        if (strcmp(sorted_order[i][4], sorted_order[i+3][4]) == 0){ // the next order is producing the same product
                                            start_dateZ = end_dateZ;
                                            total_workloadZ = total_workloadZ - atoi(sorted_order[i][3]); 
                                        } else {
                                            start_dateZ = dateCalculator(end_dateZ, 1);
                                            total_workloadZ = 0;
                                        }
                                    }
                                } else { //reject
                                    rejectedOrder[rejectedNum][0] = sorted_order[i][1]; // order number
                                    rejectedOrder[rejectedNum][1] = sorted_order[i][4]; // product name
                                    rejectedOrder[rejectedNum][2] = sorted_order[i][2]; // due date
                                    rejectedOrder[rejectedNum][3] = sorted_order[i][3]; // quantity
                                    rejectedNum++;
                                    total_workloadZ = 0;
                                }
                            }
                        }
                    }

                    // proceeds to print report
                    printREPORT(reportFile, "SJF", acceptedOrder, rejectedOrder, acceptedNum, rejectedNum,totalDay ,dayX, dayY, dayZ);
                    strcpy(buf,"SJFfinish"); // indicate the completion of child 2's operations
                    write(fd[3][1], buf, sizeof(buf));
                } else if (strcmp(buf2,"done")==0) { // child 2 terminates
                    close(fd[2][0]);close(fd[3][1]);
                    exit(0);
                }
            }
        }
    } 
    else { //Parent process
        close(fd[0][0]);close(fd[1][1]);close(fd[2][0]);close(fd[3][1]);
        printf("   ~~WELCOME TO PLS~~\n\n");
        char imp_info[2048]; // important informaiton to use in child
        while(running == true){
            // Read user input (command)
            printf("Please enter:\n  ");
            fgets(command,256,stdin);
            command[strlen(command)-1] = '\0';
            char* input = strtok(command, " ");
            for (i=0; input!=NULL; i++){ // split the command and store the words into new array
                split_cmd[i] = malloc(strlen(input) + 1);
                strcpy(split_cmd[i], input);
                input = strtok(NULL," ");
            }
            cmd = split_cmd[0]; // command for function
        
            // Go to different functions
            if (strcmp("addPERIOD",cmd) == 0){
                s_date = readDate(split_cmd[1]);
                e_date = readDate(split_cmd[2]);
                // check whether the period dates are valid
                if (validDate(s_date) == 0) {
                    printf("The period start date entered is invalid. Please enter again.\n");
                    continue;
                }
                if (validDate(e_date) == 0) {
                    printf("The period end date entered is invalid. Please enter again.\n");
                    continue;
                }
                // check whether the period range is invalid i.e. end date is set earlier than start date
                if (s_date.year > e_date.year || 
                (s_date.year == e_date.year && s_date.month > e_date.month) || 
                (s_date.year == e_date.year && s_date.month == e_date.month && s_date.day > e_date.day)) {
                    printf("The period range entered is invalid. Please enter again.\n");
                    continue;
                }
                period.start = split_cmd[1];
                period.end = split_cmd[2];
                int days = daysCalculation(s_date,e_date);
                strcpy(imp_info, split_cmd[1]);
                strcat(imp_info, " ");
                strcat(imp_info, split_cmd[2]);
            }
            else if (strcmp("addORDER",cmd) == 0){
                char* orderNum = split_cmd[1];
                char* due = split_cmd[2];
                char* quantity = split_cmd[3];
                char* productName = split_cmd[4];
                Date due_date = readDate(due);
                int co = checkOrder(due_date, quantity, s_date, e_date, productName);
                if (co > 0){
                    if (co == 1) {
                        printf("The due date entered is invalid. Please enter again.\n");
                    } else if (co == 2) {
                        printf("The due date entered is out of the period range. Please enter again.\n");
                    } else if (co == 3) {
                        printf("The quantity contains non-numberic character. Please enter again.\n");
                    } else if (co == 4) {
                        printf("The product name is invalid. Only products in the range Product_A to Product_I are accepted.\n");
                    }
                } else if (co == 0){
                    addORDER(split_cmd[0], orderNum, due, quantity, split_cmd[4]);
                }
            }
            else if (strcmp("addBATCH",cmd) == 0){
                batchfileName = split_cmd[1];
                addBATCH(batchfileName, readDate(period.start), readDate(period.end));
            }
            else if (strcmp("runPLS",cmd) == 0){
                char* algorithm = split_cmd[1];
                char* reportFile = split_cmd[5];
                char buf1[2048];
                char buf2[2048];
                if (strcmp(split_cmd[2], "|") != 0 || strcmp(split_cmd[3], "printREPORT") != 0 || strcmp(split_cmd[4], ">") != 0){
                    printf("Invalid syntax.\n");
                    continue;
                }
                if (strcmp("FCFS",algorithm) == 0){
                    strcpy(buf1,"work");
                    write(fd[0][1],buf1,sizeof(buf1)); // signal child 1 to work
                    strcpy(buf1,imp_info); 
                    strcat(buf1, " ");
                    strcat(buf1, reportFile);
                    write(fd[0][1], buf1, sizeof(buf1)); // send period dates and report file name to child 1
                    read(fd[1][0],buf,sizeof(buf)); // wait child 1 to finish
                } else if (strcmp("SJF",algorithm) == 0){
                    strcpy(buf2,"work");
                    write(fd[2][1], buf2, sizeof(buf2)); // signal child 2 to work
                    strcpy(buf2,imp_info);
                    strcpy(buf2,imp_info);
                    strcat(buf2, " ");
                    strcat(buf2, reportFile);
                    write(fd[2][1], buf2, sizeof(buf2)); // send period dates and report file name to child 2
                    read(fd[3][0], buf, sizeof(buf)); // wait child 2 to finish
                } else {
                    printf("Incorrect algorithm name. Please enter again.\n");
                }
            } 
            else if (strcmp("exitPLS",cmd) == 0){
                remove("tempOrder.txt");// Clear the tempOrder.txt
                char buf1[50];
                char buf2[50];
                strcpy(buf1, "done");
                strcpy(buf2, "done");
                write(fd[0][1], buf1, sizeof(buf1));
                write(fd[2][1], buf2, sizeof(buf2));
                wait(NULL); wait(NULL);
                close(fd[0][1]);close(fd[1][0]);close(fd[2][1]);close(fd[3][0]);
                
                printf("Bye-bye!\n");
                running = false;

                exit(0); //terminate
            } 
            else {
                printf("Error command input\n");
            }
        }
    }
    return 0;
}