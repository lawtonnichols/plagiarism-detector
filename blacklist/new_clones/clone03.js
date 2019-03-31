var towersPiles, towersMovesDone;

function TowersDisk(size) {
  this.size = size;
  this.next = null;
}

function towersPush(pile, disk) {
  var top = towersPiles[pile];
  if ((top != null) && (disk.size >= top.size))
    error("Cannot put a big disk on a smaller disk");
  disk.next = top;
  towersPiles[pile] = disk;
}

function towersPop(pile) {
  var top = towersPiles[pile];
  if (top == null) error("Attempting to remove a disk from an empty pile");
  towersPiles[pile] = top.next;
  top.next = null;
  return top;
}

function towersMoveTop(from, to) {
  towersPush(to, towersPop(from));
  towersMovesDone++;
}

function towersMove(from, to, disks) {
  if (disks == 1) {
    towersMoveTop(from, to);
  } else {
    var other = 3 - from - to;
    towersMove(from, other, disks - 1);
    towersMoveTop(from, to);
    towersMove(other, to, disks - 1);
  }
}

function towersBuild(pile, disks) {
  for (var i = disks - 1; i >= 0; i--) {
    towersPush(pile, new TowersDisk(i));
  }
}

function towers() {
  towersPiles = [ null, null, null ];
  towersBuild(0, 13);
  towersMovesDone = 0;
  towersMove(0, 1, 13);
  if (towersMovesDone != 8191) 
    error("Error in result: " + towersMovesDone + " should be: 8191");
}

towers();