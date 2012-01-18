CREATE TABLE compoundimages (
	compoundID nvarchar2(2000),
	name nvarchar2(2000),
	formula nvarchar2(2000),
	gifImageHR blob,
	gifImage64x64 blob,
	gifImage128x128 blob,
	gifImage256x256 blob,
	primary key (compoundID)
);


-- Insert only the first compound
INSERT INTO compoundimages (compoundID, name, formula)
SELECT compound, name, formula 
FROM compound x 
WHERE rowid <= (select min(rowid) from compound where compound=x.compound);

-- show duplicates
SELECT compound from compound group by compound having count(compound)>1;

-- temp
select compound, name, formula from compound x where rowid <= (select min(rowid) from compound where compound=x.compound);


CREATE TABLE compimg AS SELECT compound, name, formula FROM compound x WHERE rowid <= (select min(rowid) from compound where compound=x.compound);

INSERT INTO compoundimages (compoundID, name, formula) SELECT compound, name, formula FROM compound x WHERE rowid <= (select min(rowid) from compound where compound=x.compound);
