@Grab(group='org.asciidoctor', module='asciidoctorj', version='2.4.2')

import groovy.io.FileType

import groovy.transform.Field

import java.io.FileReader
import java.io.FileWriter

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import java.util.HashMap

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.OptionsBuilder

@Field File buildDir = new File('html')

def build() {
	Asciidoctor asciidoctor = Asciidoctor.Factory.create()
	Attributes attributes = new Attributes();
	attributes.setBackend("html5")
	HashMap<String, Object> options = OptionsBuilder.options().attributes(attributes).headerFooter(true).asMap()
	new File('src').eachFileRecurse(FileType.FILES) { file ->
		String rel = file.toString().replaceAll("\\\\", '/').replaceFirst('^src/', '')
		File target
		if (rel.endsWith('.adoc')) {
			target = new File(buildDir, rel.replaceFirst('[.]adoc$', '.html'))
			if ((!target.exists()) || (file.lastModified() > target.lastModified())) {
				File parentDir = target.getParentFile()
				if (!parentDir.exists())
					parentDir.mkdirs()
				asciidoctor.convert(new FileReader(file), new FileWriter(target), options)
				target.setLastModified(file.lastModified())
				println target
			}
		} else {
			target = new File(buildDir, rel)
			if ((!target.exists()) || (file.lastModified() > target.lastModified())) {
				File parentDir = target.getParentFile()
				if (!parentDir.exists())
					parentDir.mkdirs()
				Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
				target.setLastModified(file.lastModified())
				println target
			}
		}
	}
}

def clean() {
	buildDir.deleteDir()
}

if (args.size() > 0)
	"${args[0]}"()
else
	build()