package mil.nga.efd.validations;

import org.junit.Test;
import org.springframework.validation.annotation.Validated;

import mil.nga.efd.validations.Writable;

public class WritableTest {

	@Test
	public void testBadConstruction() {
		DataStructure ds = new DataStructure("/etc/passwd");
		ds.getPath();
	}
	
	@Validated
	public class DataStructure {
		

		private String path;
		
		public DataStructure(String path) {
			setPath(path);
		}
		
		@Writable
		public String getPath() {
			return path;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
	}
}
