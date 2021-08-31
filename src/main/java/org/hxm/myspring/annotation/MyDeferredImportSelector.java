package org.hxm.myspring.annotation;

import org.hxm.myspring.asm.MyAnnotationMetadata;

public interface MyDeferredImportSelector extends MyImportSelector{
    default Class<? extends MyDeferredImportSelector.Group> getImportGroup() {
        return null;
    }
    interface Group {

        /**
         * Process the {@link MyAnnotationMetadata} of the importing @{@link MyConfiguration}
         * class using the specified {@link MyDeferredImportSelector}.
         */
        void process(MyAnnotationMetadata metadata, MyDeferredImportSelector selector);

        /**
         * Return the {@link MyDeferredImportSelector.Group.Entry entries} of which class(es) should be imported
         * for this group.
         */
        Iterable<MyDeferredImportSelector.Group.Entry> selectImports();


        /**
         * An entry that holds the {@link MyAnnotationMetadata} of the importing
         * {@link MyConfiguration} class and the class name to import.
         */
        class Entry {

            private final MyAnnotationMetadata metadata;

            private final String importClassName;

            public Entry(MyAnnotationMetadata metadata, String importClassName) {
                this.metadata = metadata;
                this.importClassName = importClassName;
            }

            /**
             * Return the {@link MyAnnotationMetadata} of the importing
             * {@link MyConfiguration} class.
             */
            public MyAnnotationMetadata getMetadata() {
                return this.metadata;
            }

            /**
             * Return the fully qualified name of the class to import.
             */
            public String getImportClassName() {
                return this.importClassName;
            }

            @Override
            public boolean equals(Object other) {
                if (this == other) {
                    return true;
                }
                if (other == null || getClass() != other.getClass()) {
                    return false;
                }
                MyDeferredImportSelector.Group.Entry entry = (MyDeferredImportSelector.Group.Entry) other;
                return (this.metadata.equals(entry.metadata) && this.importClassName.equals(entry.importClassName));
            }

            @Override
            public int hashCode() {
                return (this.metadata.hashCode() * 31 + this.importClassName.hashCode());
            }

            @Override
            public String toString() {
                return this.importClassName;
            }
        }
    }
}
