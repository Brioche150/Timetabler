Create Table Helper
(
HelperID INTEGER PRIMARY KEY NOT NULL,
HelperName VARCHAR(80) NOT NULL,
EmailAddress VARCHAR(80),
YearGroup INTEGER,
Password Varchar(80) not null default 'Password+23'
);
Create Table Teacher
(
TeacherID INTEGER PRIMARY KEY NOT NULL,
TeacherName VARCHAR(80) NOT NULL,
EmailAddress VARCHAR(80),
Password Varchar(80) not null default 'PasswordZAQ!'
);
Create Table Administrator
(
AdministratorID INTEGER PRIMARY KEY NOT NULL,
AdministratorName VARCHAR(80) NOT NULL,
EmailAddress VARCHAR(80),
Password Varchar(80) not null default 'PasswordZAQ!'
);
Create Table Subject
(
SubjectName VARCHAR(40) PRIMARY KEY NOT NULL
);
Create Table Period
(
PeriodID INTEGER PRIMARY KEY NOT NULL,
Week CHAR(1) NOT NULL,
Day VARCHAR(9) NOT NULL,
PeriodNumber INTEGER
);
Create Table Class
(
ClassID INTEGER PRIMARY KEY NOT NULL,
SubjectName VARCHAR(40) NOT NULL,
TeacherID INTEGER NOT NULL,
ClassYear INTEGER,
Form Char(1),
FOREIGN KEY(SubjectName) references Subject(SubjectName) on delete cascade
FOREIGN KEY(TeacherID) references Teacher(TeacherID) on delete cascade
);

Create Table HelperSubject
(
HelperID INTEGER NOT NULL,
SubjectName VARCHAR(40) NOT NULL,
HelperRating INTEGER NOT NULL,
FOREIGN KEY(HelperID) references Helper(HelperID) on delete cascade,
FOREIGN KEY(SubjectName) references Subject(SubjectName) on delete cascade,
PRIMARY KEY(HelperID, SubjectName)
); 
Create Table HelperAvailability
(
HelperID INTEGER NOT NULL,
PeriodID INTEGER NOT NULL,
Willingness INTEGER NOT NULL,
PRIMARY Key(HelperID, PeriodID),
FOREIGN KEY(HelperID) references Helper(HelperID) on delete cascade,
FOREIGN KEY(PeriodID) references Period(PeriodID) on delete cascade
);
Create Table ClassTimes
(
ClassID INTEGER NOT NULL,
PeriodID INTEGER NOT NULL,
RoomNumber INTEGER,
LessonPriority INTEGER NOT NULL,
HelpersNeeded INTEGER NOT NULL,
FOREIGN KEY(ClassID) references Class(ClassID) on delete cascade,
FOREIGN KEY(PeriodID) references Period(PeriodID) on delete cascade,
PRIMARY KEY(ClassID, PeriodID)
);
Create Table Assignments
(
HelperID INTEGER NOT NULL,
PeriodID INTEGER NOT NULL,
ClassID INTEGER NOT NULL,
Cost Integer NOT NULL,
FOREIGN KEY(ClassID) references Class(ClassID) on delete cascade,
FOREIGN KEY(HelperID) references Helper(HelperID) on delete cascade,
FOREIGN KEY(PeriodID) references Period(PeriodID) on delete cascade,
PRIMARY Key(HelperID, PeriodID)
);
Create Table TempUnavailable
(
HelperID INTEGER NOT NULL,
PeriodID INTEGER NOT NULL,
FOREIGN KEY(ClassID) references Class(ClassID) on delete cascade,
FOREIGN KEY(HelperID) references Helper(HelperID) on delete cascade,
);